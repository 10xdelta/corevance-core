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
package com.corevance.commands.starter;

import com.corevance.commands.service.AuditReadPlatformService;
import com.corevance.commands.service.AuditReadPlatformServiceImpl;
import com.corevance.infrastructure.core.data.PaginationParametersDataValidator;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.PaginationHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.infrastructure.security.service.SqlValidator;
import com.corevance.infrastructure.security.utils.ColumnValidator;
import com.corevance.organisation.office.service.OfficeReadPlatformService;
import com.corevance.organisation.staff.service.StaffReadService;
import com.corevance.portfolio.client.service.ClientReadPlatformService;
import com.corevance.portfolio.loanproduct.service.LoanProductReadPlatformService;
import com.corevance.portfolio.savings.service.DepositProductReadPlatformService;
import com.corevance.portfolio.savings.service.SavingsProductReadPlatformService;
import com.corevance.useradministration.service.AppUserReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CommandsConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditReadPlatformService.class)
    public AuditReadPlatformService auditReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            FromJsonHelper fromApiJsonHelper, AppUserReadPlatformService appUserReadPlatformService,
            OfficeReadPlatformService officeReadPlatformService, ClientReadPlatformService clientReadPlatformService,
            LoanProductReadPlatformService loanProductReadPlatformService, StaffReadService staffReadPlatformService,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator,
            PaginationParametersDataValidator paginationParametersDataValidator,
            SavingsProductReadPlatformService savingsProductReadPlatformService,
            DepositProductReadPlatformService depositProductReadPlatformService, ColumnValidator columnValidator,
            SqlValidator sqlValidator) {
        return new AuditReadPlatformServiceImpl(jdbcTemplate, context, fromApiJsonHelper, appUserReadPlatformService,
                officeReadPlatformService, clientReadPlatformService, loanProductReadPlatformService, staffReadPlatformService,
                paginationHelper, sqlGenerator, paginationParametersDataValidator, savingsProductReadPlatformService,
                depositProductReadPlatformService, columnValidator, sqlValidator);
    }

}
