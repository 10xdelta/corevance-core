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
package com.corevance.useradministration.starter;

import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.security.service.PlatformPasswordEncoder;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.organisation.office.domain.OfficeRepositoryWrapper;
import com.corevance.organisation.office.service.OfficeReadPlatformService;
import com.corevance.organisation.staff.domain.StaffRepository;
import com.corevance.organisation.staff.service.StaffReadService;
import com.corevance.useradministration.data.PasswordPreferencesDataValidator;
import com.corevance.useradministration.domain.AppUserPreviousPasswordRepository;
import com.corevance.useradministration.domain.AppUserRepository;
import com.corevance.useradministration.domain.PasswordValidationPolicyRepository;
import com.corevance.useradministration.domain.PermissionRepository;
import com.corevance.useradministration.domain.RoleRepository;
import com.corevance.useradministration.domain.UserDomainService;
import com.corevance.useradministration.serialization.PermissionsCommandFromApiJsonDeserializer;
import com.corevance.useradministration.service.AppUserReadPlatformService;
import com.corevance.useradministration.service.AppUserReadPlatformServiceImpl;
import com.corevance.useradministration.service.AppUserWritePlatformService;
import com.corevance.useradministration.service.AppUserWritePlatformServiceJpaRepositoryImpl;
import com.corevance.useradministration.service.PasswordPreferencesWritePlatformService;
import com.corevance.useradministration.service.PasswordPreferencesWritePlatformServiceJpaRepositoryImpl;
import com.corevance.useradministration.service.PasswordValidationPolicyReadPlatformService;
import com.corevance.useradministration.service.PasswordValidationPolicyReadPlatformServiceImpl;
import com.corevance.useradministration.service.PermissionReadPlatformService;
import com.corevance.useradministration.service.PermissionReadPlatformServiceImpl;
import com.corevance.useradministration.service.PermissionWritePlatformService;
import com.corevance.useradministration.service.PermissionWritePlatformServiceJpaRepositoryImpl;
import com.corevance.useradministration.service.RoleDataValidator;
import com.corevance.useradministration.service.RoleReadPlatformService;
import com.corevance.useradministration.service.RoleReadPlatformServiceImpl;
import com.corevance.useradministration.service.RoleWritePlatformService;
import com.corevance.useradministration.service.RoleWritePlatformServiceJpaRepositoryImpl;
import com.corevance.useradministration.service.UserDataValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class UserAdministrationConfiguration {

    @Bean
    @ConditionalOnMissingBean(AppUserReadPlatformService.class)
    public AppUserReadPlatformService appUserReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            OfficeReadPlatformService officeReadPlatformService, RoleReadPlatformService roleReadPlatformService,
            AppUserRepository appUserRepository, StaffReadService staffReadPlatformService) {
        return new AppUserReadPlatformServiceImpl(context, jdbcTemplate, officeReadPlatformService, roleReadPlatformService,
                appUserRepository, staffReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(AppUserWritePlatformService.class)
    public AppUserWritePlatformService appUserWritePlatformService(PlatformSecurityContext context, UserDomainService userDomainService,
            PlatformPasswordEncoder platformPasswordEncoder, AppUserRepository appUserRepository,
            OfficeRepositoryWrapper officeRepositoryWrapper, RoleRepository roleRepository, UserDataValidator fromApiJsonDeserializer,
            AppUserPreviousPasswordRepository appUserPreviewPasswordRepository, StaffRepository staffRepository,
            ConfigurationDomainService configurationDomainService) {
        return new AppUserWritePlatformServiceJpaRepositoryImpl(context, userDomainService, platformPasswordEncoder, appUserRepository,
                officeRepositoryWrapper, roleRepository, fromApiJsonDeserializer, appUserPreviewPasswordRepository, staffRepository,
                configurationDomainService);
    }

    @Bean
    @ConditionalOnMissingBean(PasswordPreferencesWritePlatformService.class)
    public PasswordPreferencesWritePlatformService passwordPreferencesWritePlatformService(
            PasswordValidationPolicyRepository validationPolicyRepository, PasswordPreferencesDataValidator dataValidator) {
        return new PasswordPreferencesWritePlatformServiceJpaRepositoryImpl(validationPolicyRepository, dataValidator);
    }

    @Bean
    @ConditionalOnMissingBean(PasswordValidationPolicyReadPlatformService.class)
    public PasswordValidationPolicyReadPlatformService passwordValidationPolicyReadPlatformService(JdbcTemplate jdbcTemplate,
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new PasswordValidationPolicyReadPlatformServiceImpl(jdbcTemplate, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(PermissionReadPlatformService.class)
    public PermissionReadPlatformService permissionReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new PermissionReadPlatformServiceImpl(context, jdbcTemplate, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(PermissionWritePlatformService.class)
    public PermissionWritePlatformService permissionWritePlatformService(PlatformSecurityContext context,
            PermissionRepository permissionRepository, PermissionsCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        return new PermissionWritePlatformServiceJpaRepositoryImpl(context, permissionRepository, fromApiJsonDeserializer);
    }

    @Bean
    @ConditionalOnMissingBean(RoleReadPlatformService.class)
    public RoleReadPlatformService roleReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new RoleReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(RoleWritePlatformService.class)
    public RoleWritePlatformService roleWritePlatformService(PlatformSecurityContext context, RoleRepository roleRepository,
            PermissionRepository permissionRepository, RoleDataValidator roleCommandFromApiJsonDeserializer,
            PermissionsCommandFromApiJsonDeserializer permissionsFromApiJsonDeserializer) {
        return new RoleWritePlatformServiceJpaRepositoryImpl(context, roleRepository, permissionRepository,
                roleCommandFromApiJsonDeserializer, permissionsFromApiJsonDeserializer);
    }
}
