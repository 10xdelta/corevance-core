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

package com.corevance.portfolio.group.starter;

import com.corevance.commands.service.CommandProcessingService;
import com.corevance.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import com.corevance.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import com.corevance.infrastructure.codes.service.CodeValueReadPlatformService;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.data.PaginationParametersDataValidator;
import com.corevance.infrastructure.core.service.PaginationHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.infrastructure.security.utils.ColumnValidator;
import com.corevance.organisation.office.domain.OfficeRepositoryWrapper;
import com.corevance.organisation.office.service.OfficeReadPlatformService;
import com.corevance.organisation.staff.domain.StaffRepositoryWrapper;
import com.corevance.organisation.staff.service.StaffReadService;
import com.corevance.portfolio.account.service.AccountNumberGenerator;
import com.corevance.portfolio.calendar.domain.CalendarInstanceRepository;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.portfolio.client.service.ClientReadPlatformService;
import com.corevance.portfolio.group.domain.GroupLevelRepository;
import com.corevance.portfolio.group.domain.GroupRepositoryWrapper;
import com.corevance.portfolio.group.domain.GroupRoleRepositoryWrapper;
import com.corevance.portfolio.group.serialization.GroupRolesDataValidator;
import com.corevance.portfolio.group.serialization.GroupingTypesDataValidator;
import com.corevance.portfolio.group.service.CenterReadPlatformService;
import com.corevance.portfolio.group.service.CenterReadPlatformServiceImpl;
import com.corevance.portfolio.group.service.GroupLevelReadPlatformService;
import com.corevance.portfolio.group.service.GroupLevelReadPlatformServiceImpl;
import com.corevance.portfolio.group.service.GroupReadPlatformService;
import com.corevance.portfolio.group.service.GroupReadPlatformServiceImpl;
import com.corevance.portfolio.group.service.GroupRolesReadPlatformService;
import com.corevance.portfolio.group.service.GroupRolesReadPlatformServiceImpl;
import com.corevance.portfolio.group.service.GroupRolesWritePlatformService;
import com.corevance.portfolio.group.service.GroupRolesWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.group.service.GroupingTypesWritePlatformService;
import com.corevance.portfolio.group.service.GroupingTypesWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.service.LoanOfficerService;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class GroupConfiguration {

    @Bean
    @ConditionalOnMissingBean(CenterReadPlatformService.class)
    public CenterReadPlatformService centerReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            ClientReadPlatformService clientReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            StaffReadService staffReadPlatformService, CodeValueReadPlatformService codeValueReadPlatformService,
            ConfigurationDomainService configurationDomainService, ColumnValidator columnValidator, PaginationHelper paginationHelper,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationParametersDataValidator paginationParametersDataValidator) {
        return new CenterReadPlatformServiceImpl(jdbcTemplate, context, clientReadPlatformService, officeReadPlatformService,
                staffReadPlatformService, codeValueReadPlatformService, configurationDomainService, columnValidator, paginationHelper,
                sqlGenerator, paginationParametersDataValidator);
    }

    @Bean
    @ConditionalOnMissingBean(GroupingTypesWritePlatformService.class)
    public GroupingTypesWritePlatformService groupingTypesWritePlatformService(PlatformSecurityContext context,
            GroupRepositoryWrapper groupRepository, ClientRepositoryWrapper clientRepositoryWrapper,
            OfficeRepositoryWrapper officeRepositoryWrapper, StaffRepositoryWrapper staffRepository, NoteRepository noteRepository,
            GroupLevelRepository groupLevelRepository, GroupingTypesDataValidator fromApiJsonDeserializer,
            LoanRepositoryWrapper loanRepositoryWrapper, CodeValueRepositoryWrapper codeValueRepository,
            CommandProcessingService commandProcessingService, CalendarInstanceRepository calendarInstanceRepository,
            ConfigurationDomainService configurationDomainService, SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper,
            AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, AccountNumberGenerator accountNumberGenerator,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService,
            BusinessEventNotifierService businessEventNotifierService, LoanOfficerService loanOfficerService

    ) {
        return new GroupingTypesWritePlatformServiceJpaRepositoryImpl(context, groupRepository, clientRepositoryWrapper,
                officeRepositoryWrapper, staffRepository, noteRepository, groupLevelRepository, fromApiJsonDeserializer,
                loanRepositoryWrapper, codeValueRepository, commandProcessingService, calendarInstanceRepository,
                configurationDomainService, savingsAccountRepositoryWrapper, accountNumberFormatRepository, accountNumberGenerator,
                entityDatatableChecksWritePlatformService, businessEventNotifierService, loanOfficerService

        );
    }

    @Bean
    @ConditionalOnMissingBean(GroupLevelReadPlatformService.class)
    public GroupLevelReadPlatformService groupLevelReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate) {
        return new GroupLevelReadPlatformServiceImpl(context, jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(GroupReadPlatformService.class)
    public GroupReadPlatformService groupReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            OfficeReadPlatformService officeReadPlatformService, StaffReadService staffReadPlatformService,
            CenterReadPlatformService centerReadPlatformService, CodeValueReadPlatformService codeValueReadPlatformService,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator,
            PaginationParametersDataValidator paginationParametersDataValidator, ColumnValidator columnValidator,
            ClientReadPlatformService clientReadPlatformService) {
        return new GroupReadPlatformServiceImpl(jdbcTemplate, context, officeReadPlatformService, staffReadPlatformService,
                centerReadPlatformService, codeValueReadPlatformService, paginationHelper, sqlGenerator, paginationParametersDataValidator,
                columnValidator, clientReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(GroupRolesReadPlatformService.class)
    public GroupRolesReadPlatformService groupRolesReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context) {
        return new GroupRolesReadPlatformServiceImpl(jdbcTemplate, context);
    }

    @Bean
    @ConditionalOnMissingBean(GroupRolesWritePlatformService.class)
    public GroupRolesWritePlatformService groupRolesWritePlatformService(PlatformSecurityContext context,
            GroupRepositoryWrapper groupRepository, GroupRolesDataValidator fromApiJsonDeserializer,
            CodeValueRepositoryWrapper codeValueRepository, ClientRepositoryWrapper clientRepository,
            GroupRoleRepositoryWrapper groupRoleRepository) {
        return new GroupRolesWritePlatformServiceJpaRepositoryImpl(context, groupRepository, fromApiJsonDeserializer, codeValueRepository,
                clientRepository, groupRoleRepository);
    }
}
