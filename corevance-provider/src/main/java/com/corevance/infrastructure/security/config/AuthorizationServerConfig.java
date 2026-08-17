/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.corevance.infrastructure.security.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import com.corevance.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.domain.CorevanceRequestContextHolder;
import com.corevance.infrastructure.core.filters.CallerIpTrackingFilter;
import com.corevance.infrastructure.core.filters.CorrelationHeaderFilter;
import com.corevance.infrastructure.core.filters.IdempotencyStoreFilter;
import com.corevance.infrastructure.core.filters.IdempotencyStoreHelper;
import com.corevance.infrastructure.core.filters.RequestResponseFilter;
import com.corevance.infrastructure.core.service.MDCWrapper;
import com.corevance.infrastructure.instancemode.filter.CorevanceInstanceModeApiFilter;
import com.corevance.infrastructure.jobs.filter.LoanCOBApiFilter;
import com.corevance.infrastructure.jobs.filter.LoanCOBFilterHelper;
import com.corevance.infrastructure.jobs.filter.ProgressiveLoanModelCheckerFilter;
import com.corevance.infrastructure.security.converter.CorevanceJwtAuthenticationTokenConverter;
import com.corevance.infrastructure.security.data.TenantAuthenticationDetails;
import com.corevance.infrastructure.security.filter.BusinessDateFilter;
import com.corevance.infrastructure.security.filter.TenantAwareAuthenticationFilter;
import com.corevance.infrastructure.security.filter.TwoFactorAuthenticationFilter;
import com.corevance.infrastructure.security.service.AuthTenantDetailsService;
import com.corevance.infrastructure.security.service.TemporaryPasswordAwareAuthenticationProvider;
import com.corevance.infrastructure.security.service.TenantAwareJpaPlatformUserDetailsService;
import com.corevance.infrastructure.security.service.TwoFactorService;
import com.corevance.useradministration.domain.AppUser;
import com.corevance.useradministration.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty("corevance.security.oauth2.enabled")
@EnableConfigurationProperties(CorevanceProperties.class)
public class AuthorizationServerConfig {

    public static final String TENANT_ID = "tenantId";
    @Autowired
    private TenantAwareJpaPlatformUserDetailsService userDetailsService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MDCWrapper mdcWrapper;

    @Autowired(required = false)
    private LoanCOBFilterHelper loanCOBFilterHelper;

    @Autowired
    private IdempotencyStoreHelper idempotencyStoreHelper;

    @Autowired
    private CorevanceRequestContextHolder corevanceRequestContextHolder;

    @Autowired
    private CorevanceProperties corevanceProperties;

    @Autowired
    private AuthTenantDetailsService tenantDetailsService;

    @Autowired
    private BusinessDateReadPlatformService businessDateReadPlatformService;

    @Autowired
    ProgressiveLoanModelCheckerFilter progressiveLoanModelCheckerFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain publicEndpoints(HttpSecurity http) throws Exception {
        // Public endpoints: permitAll, no JWT
        http.securityMatcher("/swagger-ui/**", "/corevance.json", "/actuator/**", "/legacy-docs/apiLive.htm")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).csrf(AbstractHttpConfigurer::disable);

        if (corevanceProperties.getSecurity().getCors().isEnabled()) {
            http.cors(Customizer.withDefaults());
        }

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher()) // only OAuth2 endpoints
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // TODO: Make it configurable
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));
        http.with(authorizationServerConfigurer, Customizer.withDefaults());

        if (corevanceProperties.getSecurity().getCors().isEnabled()) {
            http.cors(Customizer.withDefaults());
        }

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain protectedEndpoints(HttpSecurity http) throws Exception {
        http
                // TODO: Make it configurable
                .csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(auth -> {
                    auth.anyRequest().authenticated();
                    if (corevanceProperties.getSecurity().getTwoFactor().isEnabled()) {
                        auth.anyRequest().hasAuthority("TWOFACTOR_AUTHENTICATED");
                    }
                }).authenticationProvider(customAuthenticationProvider())
                .formLogin(form -> form.loginPage("/login").authenticationDetailsSource(tenantAuthDetailsSource()).permitAll())
                .oauth2ResourceServer(
                        resourceServer -> resourceServer.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter())))
                .addFilterAfter(tenantAwareAuthenticationFilter(), SecurityContextHolderFilter.class)//
                .addFilterAfter(businessDateFilter(), TenantAwareAuthenticationFilter.class) //
                .addFilterAfter(requestResponseFilter(), ExceptionTranslationFilter.class) //
                .addFilterAfter(correlationHeaderFilter(), RequestResponseFilter.class) //
                .addFilterAfter(corevanceInstanceModeApiFilter(), CorrelationHeaderFilter.class); //
        if (!Objects.isNull(loanCOBFilterHelper)) {
            http.addFilterAfter(loanCOBApiFilter(), CorevanceInstanceModeApiFilter.class) //
                    .addFilterAfter(idempotencyStoreFilter(), LoanCOBApiFilter.class); //
            http.addFilterBefore(progressiveLoanModelCheckerFilter, LoanCOBApiFilter.class);
        } else {
            http.addFilterAfter(idempotencyStoreFilter(), CorevanceInstanceModeApiFilter.class); //
            http.addFilterAfter(progressiveLoanModelCheckerFilter, CorevanceInstanceModeApiFilter.class);
        }

        if (corevanceProperties.getIpTracking().isEnabled()) {
            http.addFilterAfter(callerIpTrackingFilter(), RequestResponseFilter.class);
        }
        if (corevanceProperties.getSecurity().getTwoFactor().isEnabled()) {
            http.addFilterAfter(twoFactorAuthenticationFilter(), CorrelationHeaderFilter.class);
        }
        if (corevanceProperties.getSecurity().getCors().isEnabled()) {
            http.cors(Customizer.withDefaults());
        }
        return http.build();
    }

    @Bean
    public OncePerRequestFilter tenantAwareAuthenticationFilter() {
        return new TenantAwareAuthenticationFilter(resolver(), tenantDetailsService);
    }

    @Bean
    public OncePerRequestFilter businessDateFilter() {
        return new BusinessDateFilter(businessDateReadPlatformService);
    }

    @Bean
    public BearerTokenResolver resolver() {
        return new DefaultBearerTokenResolver();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider customAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new TemporaryPasswordAwareAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(CorevanceProperties corevanceProperties) {

        List<RegisteredClient> clients = corevanceProperties.getSecurity().getOauth2().getClient().getRegistrations().values().stream()
                .map(reg -> {
                    return RegisteredClient.withId(UUID.randomUUID().toString()).clientId(reg.getClientId())
                            .clientAuthenticationMethods(methods -> methods.add(ClientAuthenticationMethod.NONE))
                            .scopes(scopes -> scopes.addAll(reg.getScopes()))
                            .authorizationGrantTypes(grants -> reg.getAuthorizationGrantTypes()
                                    .forEach(grant -> grants.add(new AuthorizationGrantType(grant))))
                            .redirectUris(uris -> uris.addAll(reg.getRedirectUris()))
                            .clientSettings(
                                    ClientSettings.builder().requireAuthorizationConsent(reg.isRequireAuthorizationConsent()).build())
                            .build();
                }).toList();

        return new InMemoryRegisteredClientRepository(clients);
    }

    @Bean
    @Scope("prototype")
    public AuthenticationDetailsSource<HttpServletRequest, TenantAuthenticationDetails> tenantAuthDetailsSource() {
        return request -> {
            String tenantId = request.getParameter(TENANT_ID);
            String username = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY); // "username"
            String password = request.getParameter(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_PASSWORD_KEY); // "password"
            return new TenantAuthenticationDetails(username, tenantId, password);
        };
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            UsernamePasswordAuthenticationToken authentication = context.getPrincipal();
            TenantAuthenticationDetails details = (TenantAuthenticationDetails) authentication.getDetails();
            AppUser appUser = (AppUser) authentication.getPrincipal();
            List<String> roles = appUser.getRoles().stream().map(Role::getName).toList();
            List<String> scope = appUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
            context.getClaims().claim("scope", scope).claim("role", roles).claim("tenant", details.getTenantId());
        };
    }

    @Bean
    public CorevanceJwtAuthenticationTokenConverter authenticationConverter() {
        return new CorevanceJwtAuthenticationTokenConverter(userDetailsService);
    }

    public RequestResponseFilter requestResponseFilter() {
        return new RequestResponseFilter();
    }

    public LoanCOBApiFilter loanCOBApiFilter() {
        return new LoanCOBApiFilter(loanCOBFilterHelper);
    }

    public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter() {
        TwoFactorService twoFactorService = applicationContext.getBean(TwoFactorService.class);
        return new TwoFactorAuthenticationFilter(twoFactorService);
    }

    public CorevanceInstanceModeApiFilter corevanceInstanceModeApiFilter() {
        return new CorevanceInstanceModeApiFilter(corevanceProperties);
    }

    public IdempotencyStoreFilter idempotencyStoreFilter() {
        return new IdempotencyStoreFilter(corevanceRequestContextHolder, idempotencyStoreHelper, corevanceProperties);
    }

    public CorrelationHeaderFilter correlationHeaderFilter() {
        return new CorrelationHeaderFilter(corevanceProperties, mdcWrapper);
    }

    public CallerIpTrackingFilter callerIpTrackingFilter() {
        return new CallerIpTrackingFilter(corevanceProperties);
    }
}
