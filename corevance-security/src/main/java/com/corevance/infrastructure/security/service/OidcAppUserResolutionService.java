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

import java.util.Set;
import com.corevance.useradministration.domain.AppUser;

/**
 * Resolves or auto-creates a Corevance {@link AppUser} from an OIDC identity. The interface lives in corevance-security;
 * the implementation lives in corevance-provider where AppUserRepository and RoleRepository are available.
 */
public interface OidcAppUserResolutionService {

    /**
     * Looks up an existing AppUser by username, falling back to email. If no match is found and auto-create is enabled,
     * creates a new user with the supplied attributes and the configured default roles merged with any requested roles.
     * Throws {@link com.corevance.infrastructure.security.exception.OidcUserNotFoundException} when the user is
     * not found and auto-create is disabled.
     */
    AppUser resolveOrCreate(String username, String email, String firstName, String lastName, Set<String> requestedRoles);
}
