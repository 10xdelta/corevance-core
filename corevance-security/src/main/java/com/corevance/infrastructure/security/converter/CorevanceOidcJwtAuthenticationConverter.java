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
package com.corevance.infrastructure.security.converter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.security.data.CorevanceJwtAuthenticationToken;
import com.corevance.infrastructure.security.exception.OidcUserNotFoundException;
import com.corevance.infrastructure.security.service.CorevanceOidcUserService;
import com.corevance.useradministration.domain.AppUser;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Converts a {@link Jwt} issued by an external Identity Provider into a {@link CorevanceJwtAuthenticationToken} whose
 * principal is a Corevance {@link AppUser}.
 *
 * <p>
 * Unlike {@link CorevanceJwtAuthenticationTokenConverter} (which handles JWTs issued by Corevance's own OAuth2
 * Authorization Server and always uses {@code sub} as the username), this converter reads the username from a
 * configurable claim so that external IdPs (Keycloak, Azure AD, Okta, Auth0) can be supported without modifying token
 * contents.
 *
 * <p>
 * The resulting token type is identical to the one produced by the internal converter, ensuring that
 * {@code SpringSecurityPlatformSecurityContext.authenticatedUser()} resolves the principal as {@link AppUser} without
 * any additional changes downstream.
 */
@Slf4j
@RequiredArgsConstructor
public class CorevanceOidcJwtAuthenticationConverter implements Converter<Jwt, CorevanceJwtAuthenticationToken> {

    private final CorevanceOidcUserService oidcUserService;

    @Override
    @NonNull
    public CorevanceJwtAuthenticationToken convert(@NonNull Jwt jwt) {
        String username = oidcUserService.extractUsername(jwt);

        log.debug("Converting external IdP JWT for username '{}' (subject: '{}')", username, jwt.getSubject());

        try {
            AppUser appUser = oidcUserService.resolveUser(jwt, username);
            return new CorevanceJwtAuthenticationToken(jwt, appUser.getAuthorities(), appUser);
        } catch (OidcUserNotFoundException ex) {
            log.warn("JWT conversion failed — OIDC user not found: {}", ex.getMessage());
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, ex.getMessage(), null), ex);
        }
    }
}
