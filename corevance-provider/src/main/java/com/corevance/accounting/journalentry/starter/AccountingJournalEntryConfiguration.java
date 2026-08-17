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
package com.corevance.accounting.journalentry.starter;

import com.corevance.accounting.closure.domain.GLClosureRepository;
import com.corevance.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import com.corevance.accounting.glaccount.domain.GLAccountRepository;
import com.corevance.accounting.glaccount.service.GLAccountReadPlatformService;
import com.corevance.accounting.journalentry.domain.JournalEntryRepository;
import com.corevance.accounting.journalentry.serialization.JournalEntryCommandFromApiJsonDeserializer;
import com.corevance.accounting.journalentry.service.AccountingProcessorForLoanFactory;
import com.corevance.accounting.journalentry.service.AccountingProcessorForSavingsFactory;
import com.corevance.accounting.journalentry.service.AccountingProcessorForSharesFactory;
import com.corevance.accounting.journalentry.service.AccountingProcessorHelper;
import com.corevance.accounting.journalentry.service.CashBasedAccountingProcessorForClientTransactions;
import com.corevance.accounting.journalentry.service.JournalEntryReadPlatformService;
import com.corevance.accounting.journalentry.service.JournalEntryReadPlatformServiceImpl;
import com.corevance.accounting.journalentry.service.JournalEntryWritePlatformService;
import com.corevance.accounting.journalentry.service.JournalEntryWritePlatformServiceJpaRepositoryImpl;
import com.corevance.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import com.corevance.accounting.rule.domain.AccountingRuleRepository;
import com.corevance.infrastructure.configuration.service.ConfigurationReadPlatformService;
import com.corevance.infrastructure.core.service.PaginationHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.infrastructure.security.utils.ColumnValidator;
import com.corevance.investor.domain.ExternalAssetOwnerRepository;
import com.corevance.investor.service.AccountingService;
import com.corevance.organisation.monetary.domain.OrganisationCurrencyRepositoryWrapper;
import com.corevance.organisation.office.domain.OfficeRepository;
import com.corevance.organisation.office.domain.OfficeRepositoryWrapper;
import com.corevance.organisation.office.service.OfficeReadPlatformService;
import com.corevance.portfolio.account.service.AccountTransfersReadPlatformService;
import com.corevance.portfolio.charge.domain.ChargeRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanAmortizationAllocationMappingRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AccountingJournalEntryConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccountingProcessorHelper.class)
    public AccountingProcessorHelper accountingProcessorHelper(JournalEntryRepository glJournalEntryRepository,
            ProductToGLAccountMappingRepository accountMappingRepository,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository, GLClosureRepository closureRepository,
            GLAccountRepository glAccountRepository, OfficeRepository officeRepository,
            AccountTransfersReadPlatformService accountTransfersReadPlatformService, ChargeRepositoryWrapper chargeRepositoryWrapper,
            BusinessEventNotifierService businessEventNotifierService) {
        return new AccountingProcessorHelper(glJournalEntryRepository, accountMappingRepository, financialActivityAccountRepository,
                closureRepository, glAccountRepository, officeRepository, accountTransfersReadPlatformService, chargeRepositoryWrapper,
                businessEventNotifierService);
    }

    @Bean
    @ConditionalOnMissingBean(JournalEntryReadPlatformService.class)
    public JournalEntryReadPlatformService journalEntryReadPlatformService(JdbcTemplate jdbcTemplate,
            GLAccountReadPlatformService glAccountReadPlatformService, OfficeReadPlatformService officeReadPlatformService,
            ColumnValidator columnValidator, FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new JournalEntryReadPlatformServiceImpl(jdbcTemplate, glAccountReadPlatformService, officeReadPlatformService,
                columnValidator, financialActivityAccountRepositoryWrapper, paginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(JournalEntryWritePlatformService.class)
    public JournalEntryWritePlatformService journalEntryWritePlatformService(GLClosureRepository glClosureRepository,
            GLAccountRepository glAccountRepository, JournalEntryRepository glJournalEntryRepository,
            OfficeRepositoryWrapper officeRepositoryWrapper, AccountingProcessorForLoanFactory accountingProcessorForLoanFactory,
            AccountingProcessorForSavingsFactory accountingProcessorForSavingsFactory,
            AccountingProcessorForSharesFactory accountingProcessorForSharesFactory, AccountingProcessorHelper helper,
            JournalEntryCommandFromApiJsonDeserializer fromApiJsonDeserializer, AccountingRuleRepository accountingRuleRepository,
            GLAccountReadPlatformService glAccountReadPlatformService, OrganisationCurrencyRepositoryWrapper organisationCurrencyRepository,
            PlatformSecurityContext context, PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper,
            CashBasedAccountingProcessorForClientTransactions accountingProcessorForClientTransactions,
            ConfigurationReadPlatformService configurationReadPlatformService, AccountingService accountingService,
            ExternalAssetOwnerRepository externalAssetOwnerRepository,
            LoanAmortizationAllocationMappingRepository loanAmortizationAllocationMappingRepository,
            LoanTransactionRepository loanTransactionRepository) {
        return new JournalEntryWritePlatformServiceJpaRepositoryImpl(glClosureRepository, glAccountRepository, glJournalEntryRepository,
                officeRepositoryWrapper, accountingProcessorForLoanFactory, accountingProcessorForSavingsFactory,
                accountingProcessorForSharesFactory, helper, fromApiJsonDeserializer, accountingRuleRepository,
                glAccountReadPlatformService, organisationCurrencyRepository, context, paymentDetailWritePlatformService,
                financialActivityAccountRepositoryWrapper, accountingProcessorForClientTransactions, configurationReadPlatformService,
                accountingService, externalAssetOwnerRepository, loanAmortizationAllocationMappingRepository, loanTransactionRepository);
    }
}
