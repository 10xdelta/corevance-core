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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.security.exception.OidcUserNotFoundException;
import com.corevance.organisation.office.domain.Office;
import com.corevance.organisation.office.domain.OfficeRepository;
import com.corevance.useradministration.domain.AppUser;
import com.corevance.useradministration.domain.AppUserRepository;
import com.corevance.useradministration.domain.Role;
import com.corevance.useradministration.domain.RoleRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OidcAppUserResolutionServiceImpl implements OidcAppUserResolutionService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final OfficeRepository officeRepository;
    private final CorevanceProperties corevanceProperties;

    // Stateless encoder — safe to create once per class
    private static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Override
    @Transactional
    public AppUser resolveOrCreate(String username, String email, String firstName, String lastName, Set<String> requestedRoles) {

        // 1. Lookup by username
        AppUser user = appUserRepository.findAppUserByName(username);
        if (user != null) {
            log.debug("OIDC user resolved by username: '{}'", username);
            return user;
        }

        // 2. Fallback: lookup by email
        if (email != null) {
            user = appUserRepository.findActiveUserByEmail(email);
            if (user != null) {
                log.debug("OIDC user resolved by email: '{}'", email);
                return user;
            }
        }

        // 3. Auto-create when enabled
        CorevanceProperties.CorevanceSecurityProperties.CorevanceSecurityOidcFederationProperties oidcConfig = corevanceProperties.getSecurity()
                .getOidcFederation();

        if (!oidcConfig.isAutoCreateUser()) {
            log.warn("OIDC user '{}' not found in Corevance and auto-create is disabled", username);
            throw new OidcUserNotFoundException(username);
        }

        log.info("Auto-creating Corevance user for OIDC subject '{}'", username);
        return createUser(username, email, firstName, lastName, requestedRoles, oidcConfig);
    }

    private AppUser createUser(String username, String email, String firstName, String lastName, Set<String> requestedRoles,
            CorevanceProperties.CorevanceSecurityProperties.CorevanceSecurityOidcFederationProperties oidcConfig) {

        final Office headOffice = officeRepository.findById(corevanceProperties.getDefaults().getOfficeId())
                .orElseThrow(() -> new IllegalStateException("Head office (id=1) not found — cannot auto-create OIDC user"));

        String encodedPassword = PASSWORD_ENCODER.encode(new RandomPasswordGenerator(20).generate());

        User springUser = new User(username, encodedPassword, true, true, true, true, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        Set<Role> roles = resolveRoles(oidcConfig.getDefaultRoles(), requestedRoles);

        String resolvedEmail = email != null ? email : username + "@oidc.placeholder";
        String resolvedFirstName = firstName != null ? firstName : username;
        String resolvedLastName = lastName != null ? lastName : "";

        AppUser appUser = new AppUser(headOffice, springUser, roles, resolvedEmail, resolvedFirstName, resolvedLastName, null, true, false);

        AppUser saved = appUserRepository.saveAndFlush(appUser);
        log.info("Auto-created Corevance user '{}' (id={}) from OIDC identity", username, saved.getId());
        return saved;
    }

    private Set<Role> resolveRoles(String defaultRolesConfig, Set<String> requestedRoles) {
        Set<Role> result = new HashSet<>();
        Stream.concat(Arrays.stream(defaultRolesConfig.split(",")), requestedRoles.stream()).map(String::trim)
                .filter(name -> !name.isEmpty()).forEach(name -> {
                    Role role = roleRepository.getRoleByName(name);
                    if (role != null) {
                        result.add(role);
                    } else {
                        log.warn("OIDC role mapping: role '{}' not found in Corevance — skipping", name);
                    }
                });
        return result;
    }
}
