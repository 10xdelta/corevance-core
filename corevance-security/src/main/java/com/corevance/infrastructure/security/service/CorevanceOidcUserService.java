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
package com.corevance.infrastructure.security.service;

import java.util.Collection;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.security.data.CorevanceOidcUser;
import com.corevance.useradministration.domain.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/**
 * Maps OIDC identities (browser login or Bearer JWT) to Corevance {@link AppUser} entities. User resolution and
 * auto-creation are delegated to {@link OidcAppUserResolutionService} whose implementation lives in corevance-provider.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CorevanceOidcUserService {

    private final OidcAppUserResolutionService resolutionService;
    private final CorevanceProperties corevanceProperties;

    /**
     * Resolves the Corevance AppUser for a Bearer JWT from an external IdP. Used by
     * {@code CorevanceOidcJwtAuthenticationConverter}.
     */
    public AppUser resolveUser(Jwt jwt, String username) {
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        log.debug("Resolving Corevance user for OIDC subject '{}' (username claim: '{}')", jwt.getSubject(), username);

        return resolutionService.resolveOrCreate(username, email, firstName, lastName, Set.of());
    }

    /**
     * Resolves the Corevance AppUser for a browser-based OIDC login and wraps the result in a {@link CorevanceOidcUser}.
     * Used by the OAuth2 login redirect flow.
     */
    public CorevanceOidcUser processOidcUser(OidcUser oidcUser, String tenantId) {
        String usernameClaim = corevanceProperties.getSecurity().getOidcFederation().getUsernameClaim();

        String username = oidcUser.getClaimAsString(usernameClaim);
        if (username == null) {
            username = oidcUser.getSubject();
        }

        String email = oidcUser.getEmail();
        String firstName = oidcUser.getGivenName();
        String lastName = oidcUser.getFamilyName();

        log.debug("Processing OIDC user '{}' for tenant '{}'", username, tenantId);

        AppUser appUser = resolutionService.resolveOrCreate(username, email, firstName, lastName, Set.of());

        Collection<? extends GrantedAuthority> authorities = appUser.getAuthorities();
        OidcIdToken idToken = oidcUser.getIdToken();
        OidcUserInfo userInfo = oidcUser.getUserInfo();

        return new CorevanceOidcUser(authorities, idToken, userInfo, appUser, tenantId);
    }

    /**
     * Extracts the username from a JWT using the configured claim, falling back to {@code sub}.
     */
    public String extractUsername(Jwt jwt) {
        String claimName = corevanceProperties.getSecurity().getOidcFederation().getUsernameClaim();
        String username = jwt.getClaimAsString(claimName);
        return username != null ? username : jwt.getSubject();
    }

    /**
     * Extracts the tenant ID from a JWT using the configured claim. Returns null if the claim is absent — the filter
     * layer handles fallback to header / query param.
     */
    public String extractTenantId(Jwt jwt) {
        String claimName = corevanceProperties.getSecurity().getOidcFederation().getTenantClaimName();
        return jwt.getClaimAsString(claimName);
    }
}
