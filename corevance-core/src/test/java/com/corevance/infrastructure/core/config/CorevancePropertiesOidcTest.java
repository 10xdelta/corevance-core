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
package com.corevance.infrastructure.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.corevance.infrastructure.core.config.CorevanceProperties.CorevanceSecurityProperties.CorevanceSecurityOidcFederationProperties;
import org.junit.jupiter.api.Test;

class CorevancePropertiesOidcTest {

    @Test
    void defaultEnabledIsFalse() {
        assertThat(new CorevanceSecurityOidcFederationProperties().isEnabled()).isFalse();
    }

    @Test
    void defaultUsernameClaimIsPreferredUsername() {
        assertThat(new CorevanceSecurityOidcFederationProperties().getUsernameClaim()).isEqualTo("preferred_username");
    }

    @Test
    void defaultTenantClaimName() {
        assertThat(new CorevanceSecurityOidcFederationProperties().getTenantClaimName()).isEqualTo("corevance_tenant");
    }

    @Test
    void defaultAutoCreateUserIsFalse() {
        assertThat(new CorevanceSecurityOidcFederationProperties().isAutoCreateUser()).isFalse();
    }

    @Test
    void defaultProviderIsGeneric() {
        assertThat(new CorevanceSecurityOidcFederationProperties().getProvider().getCode()).isEqualTo("generic");
    }

    @Test
    void defaultDefaultRolesIsEmpty() {
        assertThat(new CorevanceSecurityOidcFederationProperties().getDefaultRoles()).isEmpty();
    }

    @Test
    void defaultPostLogoutRedirectUriIsNull() {
        assertThat(new CorevanceSecurityOidcFederationProperties().getPostLogoutRedirectUri()).isNull();
    }
}
