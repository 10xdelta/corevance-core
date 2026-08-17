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
package com.corevance.portfolio.account.starter;

import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.infrastructure.core.service.PaginationHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.security.service.SqlValidator;
import com.corevance.infrastructure.security.utils.ColumnValidator;
import com.corevance.organisation.office.service.OfficeReadPlatformService;
import com.corevance.portfolio.account.data.AccountTransfersDataValidator;
import com.corevance.portfolio.account.data.StandingInstructionDataValidator;
import com.corevance.portfolio.account.domain.AccountTransferAssembler;
import com.corevance.portfolio.account.domain.AccountTransferDetailRepository;
import com.corevance.portfolio.account.domain.AccountTransferRepository;
import com.corevance.portfolio.account.domain.StandingInstructionAssembler;
import com.corevance.portfolio.account.domain.StandingInstructionRepository;
import com.corevance.portfolio.account.mapper.AccountTransfersMapper;
import com.corevance.portfolio.account.service.AccountAssociationsReadPlatformService;
import com.corevance.portfolio.account.service.AccountAssociationsReadPlatformServiceImpl;
import com.corevance.portfolio.account.service.AccountTransfersReadPlatformService;
import com.corevance.portfolio.account.service.AccountTransfersReadPlatformServiceImpl;
import com.corevance.portfolio.account.service.AccountTransfersWritePlatformService;
import com.corevance.portfolio.account.service.AccountTransfersWritePlatformServiceImpl;
import com.corevance.portfolio.account.service.PortfolioAccountReadPlatformService;
import com.corevance.portfolio.account.service.PortfolioAccountReadPlatformServiceImpl;
import com.corevance.portfolio.account.service.StandingInstructionHistoryReadService;
import com.corevance.portfolio.account.service.StandingInstructionHistoryReadServiceImpl;
import com.corevance.portfolio.account.service.StandingInstructionReadPlatformService;
import com.corevance.portfolio.account.service.StandingInstructionReadPlatformServiceImpl;
import com.corevance.portfolio.account.service.StandingInstructionWritePlatformService;
import com.corevance.portfolio.account.service.StandingInstructionWritePlatformServiceImpl;
import com.corevance.portfolio.client.service.ClientReadPlatformService;
import com.corevance.portfolio.common.service.DropdownReadPlatformService;
import com.corevance.portfolio.loanaccount.domain.LoanAccountDomainService;
import com.corevance.portfolio.loanaccount.service.LoanAssembler;
import com.corevance.portfolio.loanaccount.service.LoanReadPlatformService;
import com.corevance.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import com.corevance.portfolio.savings.domain.GSIMRepositoy;
import com.corevance.portfolio.savings.domain.SavingsAccountAssembler;
import com.corevance.portfolio.savings.service.SavingsAccountDomainService;
import com.corevance.portfolio.savings.service.SavingsAccountWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AccountConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccountAssociationsReadPlatformService.class)
    public AccountAssociationsReadPlatformService accountAssociationsReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new AccountAssociationsReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(AccountTransfersReadPlatformService.class)
    public AccountTransfersReadPlatformService accountTransfersReadPlatformService(JdbcTemplate jdbcTemplate,
            ClientReadPlatformService clientReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            PortfolioAccountReadPlatformService portfolioAccountReadPlatformService, ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, AccountTransfersMapper accountTransfersMapper, PaginationHelper paginationHelper,
            SqlValidator sqlValidator) {
        return new AccountTransfersReadPlatformServiceImpl(jdbcTemplate, clientReadPlatformService, officeReadPlatformService,
                portfolioAccountReadPlatformService, columnValidator, sqlGenerator, accountTransfersMapper, paginationHelper, sqlValidator);
    }

    @Bean
    @ConditionalOnMissingBean(AccountTransfersWritePlatformService.class)
    public AccountTransfersWritePlatformService accountTransfersWritePlatformService(
            AccountTransfersDataValidator accountTransfersDataValidator, AccountTransferAssembler accountTransferAssembler,
            AccountTransferRepository accountTransferRepository, SavingsAccountAssembler savingsAccountAssembler,
            SavingsAccountDomainService savingsAccountDomainService, LoanAssembler loanAccountAssembler,
            LoanAccountDomainService loanAccountDomainService, SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            AccountTransferDetailRepository accountTransferDetailRepository, LoanReadPlatformService loanReadPlatformService,
            GSIMRepositoy gsimRepository, ConfigurationDomainService configurationDomainService, ExternalIdFactory externalIdFactory,
            CorevanceProperties corevanceProperties, LoanAdjustmentService loanAdjustmentService) {
        return new AccountTransfersWritePlatformServiceImpl(accountTransfersDataValidator, accountTransferAssembler,
                accountTransferRepository, savingsAccountAssembler, savingsAccountDomainService, loanAccountAssembler,
                loanAccountDomainService, savingsAccountWritePlatformService, accountTransferDetailRepository, loanReadPlatformService,
                gsimRepository, configurationDomainService, externalIdFactory, corevanceProperties, loanAdjustmentService);
    }

    @Bean
    @ConditionalOnMissingBean(PortfolioAccountReadPlatformService.class)
    public PortfolioAccountReadPlatformService portfolioAccountReadPlatformService(JdbcTemplate jdbcTemplate,
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new PortfolioAccountReadPlatformServiceImpl(jdbcTemplate, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(StandingInstructionHistoryReadService.class)
    public StandingInstructionHistoryReadService standingInstructionHistoryReadService(JdbcTemplate jdbcTemplate,
            ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        return new StandingInstructionHistoryReadServiceImpl(jdbcTemplate, columnValidator, sqlGenerator, paginationHelper);
    }

    @Bean
    @ConditionalOnMissingBean(StandingInstructionReadPlatformService.class)
    public StandingInstructionReadPlatformService standingInstructionReadPlatformService(JdbcTemplate jdbcTemplate,
            ClientReadPlatformService clientReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            PortfolioAccountReadPlatformService portfolioAccountReadPlatformService,
            DropdownReadPlatformService dropdownReadPlatformService, ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        return new StandingInstructionReadPlatformServiceImpl(jdbcTemplate, clientReadPlatformService, officeReadPlatformService,
                portfolioAccountReadPlatformService, dropdownReadPlatformService, columnValidator, sqlGenerator, paginationHelper);
    }

    @Bean
    @ConditionalOnMissingBean(StandingInstructionWritePlatformService.class)
    public StandingInstructionWritePlatformService standingInstructionWritePlatformService(
            StandingInstructionDataValidator standingInstructionDataValidator, StandingInstructionAssembler standingInstructionAssembler,
            AccountTransferDetailRepository accountTransferDetailRepository, StandingInstructionRepository standingInstructionRepository) {
        return new StandingInstructionWritePlatformServiceImpl(standingInstructionDataValidator, standingInstructionAssembler,
                accountTransferDetailRepository, standingInstructionRepository);
    }
}
