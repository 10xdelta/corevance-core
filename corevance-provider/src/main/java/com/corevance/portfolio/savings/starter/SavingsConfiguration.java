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
package com.corevance.portfolio.savings.starter;

import com.corevance.accounting.journalentry.service.JournalEntryWritePlatformService;
import com.corevance.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import com.corevance.commands.service.CommandProcessingService;
import com.corevance.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import com.corevance.infrastructure.codes.service.CodeValueReadPlatformService;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.data.PaginationParametersDataValidator;
import com.corevance.infrastructure.core.exception.ErrorHandler;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.infrastructure.core.service.PaginationHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.dataqueries.data.DataTableValidator;
import com.corevance.infrastructure.dataqueries.service.DatatableReadService;
import com.corevance.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import com.corevance.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import com.corevance.infrastructure.dataqueries.service.GenericDataService;
import com.corevance.infrastructure.entityaccess.service.CorevanceEntityAccessUtil;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.infrastructure.security.utils.ColumnValidator;
import com.corevance.organisation.holiday.domain.HolidayRepositoryWrapper;
import com.corevance.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import com.corevance.organisation.staff.domain.StaffRepositoryWrapper;
import com.corevance.organisation.staff.service.StaffReadService;
import com.corevance.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import com.corevance.portfolio.account.domain.AccountAssociationsRepository;
import com.corevance.portfolio.account.domain.StandingInstructionRepository;
import com.corevance.portfolio.account.service.AccountAssociationsReadPlatformService;
import com.corevance.portfolio.account.service.AccountNumberGenerator;
import com.corevance.portfolio.account.service.AccountTransfersReadPlatformService;
import com.corevance.portfolio.account.service.AccountTransfersWritePlatformService;
import com.corevance.portfolio.calendar.domain.CalendarInstanceRepository;
import com.corevance.portfolio.calendar.service.CalendarReadPlatformService;
import com.corevance.portfolio.charge.domain.ChargeRepositoryWrapper;
import com.corevance.portfolio.charge.service.ChargeDropdownReadPlatformService;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.portfolio.client.service.ClientReadPlatformService;
import com.corevance.portfolio.common.service.DropdownReadPlatformService;
import com.corevance.portfolio.group.domain.GroupRepository;
import com.corevance.portfolio.group.domain.GroupRepositoryWrapper;
import com.corevance.portfolio.group.service.GroupReadPlatformService;
import com.corevance.portfolio.interestratechart.service.InterestIncentiveDropdownReadService;
import com.corevance.portfolio.interestratechart.service.InterestRateChartAssembler;
import com.corevance.portfolio.interestratechart.service.InterestRateChartDropdownReadService;
import com.corevance.portfolio.interestratechart.service.InterestRateChartReadService;
import com.corevance.portfolio.loanaccount.domain.LoanRepository;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import com.corevance.portfolio.paymenttype.service.PaymentTypeReadService;
import com.corevance.portfolio.savings.data.DepositAccountDataValidator;
import com.corevance.portfolio.savings.data.DepositAccountTransactionDataValidator;
import com.corevance.portfolio.savings.data.DepositProductDataValidator;
import com.corevance.portfolio.savings.data.SavingsAccountChargeDataValidator;
import com.corevance.portfolio.savings.data.SavingsAccountDataValidator;
import com.corevance.portfolio.savings.data.SavingsAccountTransactionDataValidator;
import com.corevance.portfolio.savings.data.SavingsProductDataValidator;
import com.corevance.portfolio.savings.domain.DepositAccountAssembler;
import com.corevance.portfolio.savings.domain.DepositAccountDomainService;
import com.corevance.portfolio.savings.domain.DepositAccountOnHoldTransactionRepository;
import com.corevance.portfolio.savings.domain.DepositProductAssembler;
import com.corevance.portfolio.savings.domain.FixedDepositAccountRepository;
import com.corevance.portfolio.savings.domain.FixedDepositProductRepository;
import com.corevance.portfolio.savings.domain.GSIMRepositoy;
import com.corevance.portfolio.savings.domain.RecurringDepositAccountRepository;
import com.corevance.portfolio.savings.domain.RecurringDepositProductRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountAssembler;
import com.corevance.portfolio.savings.domain.SavingsAccountChargeAssembler;
import com.corevance.portfolio.savings.domain.SavingsAccountChargeRepositoryWrapper;
import com.corevance.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import com.corevance.portfolio.savings.domain.SavingsAccountTransactionRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import com.corevance.portfolio.savings.domain.SavingsHelper;
import com.corevance.portfolio.savings.domain.SavingsProductAssembler;
import com.corevance.portfolio.savings.domain.SavingsProductRepository;
import com.corevance.portfolio.savings.service.DepositAccountInterestRateChartReadPlatformService;
import com.corevance.portfolio.savings.service.DepositAccountInterestRateChartReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.DepositAccountOnHoldTransactionReadPlatformService;
import com.corevance.portfolio.savings.service.DepositAccountOnHoldTransactionReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.DepositAccountPreMatureCalculationPlatformService;
import com.corevance.portfolio.savings.service.DepositAccountPreMatureCalculationPlatformServiceImpl;
import com.corevance.portfolio.savings.service.DepositAccountReadPlatformService;
import com.corevance.portfolio.savings.service.DepositAccountReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.DepositAccountWritePlatformService;
import com.corevance.portfolio.savings.service.DepositAccountWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.savings.service.DepositApplicationProcessWritePlatformService;
import com.corevance.portfolio.savings.service.DepositApplicationProcessWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.savings.service.DepositProductReadPlatformService;
import com.corevance.portfolio.savings.service.DepositProductReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.DepositsDropdownReadPlatformService;
import com.corevance.portfolio.savings.service.DepositsDropdownReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.FixedDepositProductWritePlatformService;
import com.corevance.portfolio.savings.service.FixedDepositProductWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.savings.service.GSIMReadPlatformService;
import com.corevance.portfolio.savings.service.GSIMReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.GroupSavingsIndividualMonitoringWritePlatformService;
import com.corevance.portfolio.savings.service.GroupSavingsIndividualMonitoringWritePlatformServiceImpl;
import com.corevance.portfolio.savings.service.RecurringDepositProductWritePlatformService;
import com.corevance.portfolio.savings.service.RecurringDepositProductWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.savings.service.SavingsAccountActivationService;
import com.corevance.portfolio.savings.service.SavingsAccountActivationServiceImpl;
import com.corevance.portfolio.savings.service.SavingsAccountApplicationTransitionApiJsonValidator;
import com.corevance.portfolio.savings.service.SavingsAccountChargeReadPlatformService;
import com.corevance.portfolio.savings.service.SavingsAccountChargeReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.SavingsAccountDomainService;
import com.corevance.portfolio.savings.service.SavingsAccountInterestPostingService;
import com.corevance.portfolio.savings.service.SavingsAccountInterestPostingServiceImpl;
import com.corevance.portfolio.savings.service.SavingsAccountPostInterestService;
import com.corevance.portfolio.savings.service.SavingsAccountPostInterestServiceImpl;
import com.corevance.portfolio.savings.service.SavingsAccountReadPlatformService;
import com.corevance.portfolio.savings.service.SavingsAccountReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.SavingsAccountTemplateReadPlatformService;
import com.corevance.portfolio.savings.service.SavingsAccountTemplateReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.SavingsAccountWritePlatformService;
import com.corevance.portfolio.savings.service.SavingsAccountWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.savings.service.SavingsApplicationProcessWritePlatformService;
import com.corevance.portfolio.savings.service.SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.savings.service.SavingsDropdownReadPlatformService;
import com.corevance.portfolio.savings.service.SavingsDropdownReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.SavingsProductReadPlatformService;
import com.corevance.portfolio.savings.service.SavingsProductReadPlatformServiceImpl;
import com.corevance.portfolio.savings.service.SavingsProductWritePlatformService;
import com.corevance.portfolio.savings.service.SavingsProductWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.savings.service.SavingsSchedularInterestPoster;
import com.corevance.portfolio.savings.service.SavingsSchedularInterestPosterTask;
import com.corevance.portfolio.savings.service.search.SavingsAccountTransactionSearchService;
import com.corevance.portfolio.savings.service.search.SavingsAccountTransactionsSearchServiceImpl;
import com.corevance.portfolio.search.service.SearchUtil;
import com.corevance.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class SavingsConfiguration {

    @Bean
    @ConditionalOnMissingBean(SavingsAccountTransactionSearchService.class)
    public SavingsAccountTransactionSearchService savingsAccountTransactionSearchService(PlatformSecurityContext context,
            GenericDataService genericDataService, DatabaseSpecificSQLGenerator sqlGenerator, DatatableReadService datatableService,
            DataTableValidator dataTableValidator, JdbcTemplate jdbcTemplate, SearchUtil searchUtil) {
        return new SavingsAccountTransactionsSearchServiceImpl(context, genericDataService, sqlGenerator, datatableService,
                dataTableValidator, jdbcTemplate, searchUtil);
    }

    @Bean
    public DepositAccountInterestRateChartReadPlatformServiceImpl.DepositAccountInterestRateChartExtractor depositAccountInterestRateChartExtractor(
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new DepositAccountInterestRateChartReadPlatformServiceImpl.DepositAccountInterestRateChartExtractor(sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountInterestRateChartReadPlatformService.class)
    public DepositAccountInterestRateChartReadPlatformService depositAccountInterestRateChartReadService(PlatformSecurityContext context,
            JdbcTemplate jdbcTemplate,
            DepositAccountInterestRateChartReadPlatformServiceImpl.DepositAccountInterestRateChartExtractor chartExtractor,
            InterestRateChartDropdownReadService chartDropdownReadPlatformService,
            InterestIncentiveDropdownReadService interestIncentiveDropdownReadPlatformService,
            CodeValueReadPlatformService codeValueReadPlatformService) {
        return new DepositAccountInterestRateChartReadPlatformServiceImpl(context, jdbcTemplate, chartExtractor,
                chartDropdownReadPlatformService, interestIncentiveDropdownReadPlatformService, codeValueReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountOnHoldTransactionReadPlatformService.class)
    public DepositAccountOnHoldTransactionReadPlatformService depositAccountOnHoldTransactionReadPlatformService(JdbcTemplate jdbcTemplate,
            ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper) {
        return new DepositAccountOnHoldTransactionReadPlatformServiceImpl(jdbcTemplate, sqlGenerator, columnValidator, paginationHelper);
    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountPreMatureCalculationPlatformService.class)
    public DepositAccountPreMatureCalculationPlatformService depositAccountPreMatureCalculationPlatformService(
            FromJsonHelper fromJsonHelper, DepositAccountTransactionDataValidator depositAccountTransactionDataValidator,
            DepositAccountAssembler depositAccountAssembler, SavingsAccountReadPlatformService savingsAccountReadPlatformService,
            ConfigurationDomainService configurationDomainService, PaymentTypeReadService paymentTypeReadPlatformService) {
        return new DepositAccountPreMatureCalculationPlatformServiceImpl(fromJsonHelper, depositAccountTransactionDataValidator,
                depositAccountAssembler, savingsAccountReadPlatformService, configurationDomainService, paymentTypeReadPlatformService);

    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountReadPlatformService.class)
    public DepositAccountReadPlatformService depositAccountReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            DepositAccountInterestRateChartReadPlatformService chartReadPlatformService,
            InterestRateChartReadService productChartReadPlatformService,
            PaginationParametersDataValidator paginationParametersDataValidator, DatabaseSpecificSQLGenerator sqlGenerator,
            PaginationHelper paginationHelper, ClientReadPlatformService clientReadPlatformService,
            GroupReadPlatformService groupReadPlatformService, DepositProductReadPlatformService depositProductReadPlatformService,
            SavingsDropdownReadPlatformService savingsDropdownReadPlatformService, ChargeReadPlatformService chargeReadPlatformService,
            StaffReadService staffReadPlatformService, DepositsDropdownReadPlatformService depositsDropdownReadPlatformService,
            SavingsAccountReadPlatformService savingsAccountReadPlatformService, DropdownReadPlatformService dropdownReadPlatformService,
            CalendarReadPlatformService calendarReadPlatformService, PaymentTypeReadService paymentTypeReadPlatformService) {
        return new DepositAccountReadPlatformServiceImpl(context, jdbcTemplate, chartReadPlatformService, productChartReadPlatformService,
                paginationParametersDataValidator, sqlGenerator, paginationHelper, clientReadPlatformService, groupReadPlatformService,
                depositProductReadPlatformService, savingsDropdownReadPlatformService, chargeReadPlatformService, staffReadPlatformService,
                depositsDropdownReadPlatformService, savingsAccountReadPlatformService, dropdownReadPlatformService,
                calendarReadPlatformService, paymentTypeReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(DepositAccountWritePlatformService.class)
    public DepositAccountWritePlatformService depositAccountWritePlatformService(PlatformSecurityContext context,
            SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper,
            SavingsAccountTransactionRepository savingsAccountTransactionRepository, DepositAccountAssembler depositAccountAssembler,
            SavingsAccountPostInterestService savingsAccountPostInterestService,
            DepositAccountTransactionDataValidator depositAccountTransactionDataValidator,
            SavingsAccountChargeDataValidator savingsAccountChargeDataValidator,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            ApplicationCurrencyRepositoryWrapper applicationCurrencyRepositoryWrapper,
            JournalEntryWritePlatformService journalEntryWritePlatformService, DepositAccountDomainService depositAccountDomainService,
            NoteRepository noteRepository, AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            ChargeRepositoryWrapper chargeRepository, SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            AccountTransfersWritePlatformService accountTransfersWritePlatformService,
            DepositAccountReadPlatformService depositAccountReadPlatformService, CalendarInstanceRepository calendarInstanceRepository,
            ConfigurationDomainService configurationDomainService, HolidayRepositoryWrapper holidayRepository,
            WorkingDaysRepositoryWrapper workingDaysRepository,
            DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository

    ) {
        return new DepositAccountWritePlatformServiceJpaRepositoryImpl(context, savingAccountRepositoryWrapper,
                savingsAccountTransactionRepository, depositAccountAssembler, savingsAccountPostInterestService,
                depositAccountTransactionDataValidator, savingsAccountChargeDataValidator, paymentDetailWritePlatformService,
                applicationCurrencyRepositoryWrapper, journalEntryWritePlatformService, depositAccountDomainService, noteRepository,
                accountTransfersReadPlatformService, chargeRepository, savingsAccountChargeRepository,
                accountAssociationsReadPlatformService, accountTransfersWritePlatformService, depositAccountReadPlatformService,
                calendarInstanceRepository, configurationDomainService, holidayRepository, workingDaysRepository,
                depositAccountOnHoldTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(DepositApplicationProcessWritePlatformService.class)
    public DepositApplicationProcessWritePlatformService depositApplicationProcessWritePlatformService(PlatformSecurityContext context,
            SavingsAccountRepositoryWrapper savingAccountRepository, FixedDepositAccountRepository fixedDepositAccountRepository,
            RecurringDepositAccountRepository recurringDepositAccountRepository, DepositAccountAssembler depositAccountAssembler,
            DepositAccountDataValidator depositAccountDataValidator, AccountNumberGenerator accountNumberGenerator,
            ClientRepositoryWrapper clientRepository, GroupRepository groupRepository, SavingsProductRepository savingsProductRepository,
            NoteRepository noteRepository, StaffRepositoryWrapper staffRepository,
            SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator,
            SavingsAccountChargeAssembler savingsAccountChargeAssembler, AccountAssociationsRepository accountAssociationsRepository,
            FromJsonHelper fromJsonHelper, CalendarInstanceRepository calendarInstanceRepository,
            ConfigurationDomainService configurationDomainService, AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            BusinessEventNotifierService businessEventNotifierService) {
        return new DepositApplicationProcessWritePlatformServiceJpaRepositoryImpl(context, savingAccountRepository,
                fixedDepositAccountRepository, recurringDepositAccountRepository, depositAccountAssembler, depositAccountDataValidator,
                accountNumberGenerator, clientRepository, groupRepository, savingsProductRepository, noteRepository, staffRepository,
                savingsAccountApplicationTransitionApiJsonValidator, savingsAccountChargeAssembler, accountAssociationsRepository,
                fromJsonHelper, calendarInstanceRepository, configurationDomainService, accountNumberFormatRepository,
                businessEventNotifierService);
    }

    @Bean
    @ConditionalOnMissingBean(DepositProductReadPlatformService.class)
    public DepositProductReadPlatformService depositProductReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            InterestRateChartReadService interestRateChartReadPlatformService) {
        return new DepositProductReadPlatformServiceImpl(context, jdbcTemplate, interestRateChartReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(DepositsDropdownReadPlatformService.class)
    public DepositsDropdownReadPlatformService depositsDropdownReadPlatformService() {
        return new DepositsDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(FixedDepositProductWritePlatformService.class)
    public FixedDepositProductWritePlatformService fixedDepositProductWritePlatformService(PlatformSecurityContext context,
            FixedDepositProductRepository fixedDepositProductRepository, DepositProductDataValidator fromApiJsonDataValidator,
            DepositProductAssembler depositProductAssembler,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService, InterestRateChartAssembler chartAssembler) {
        return new FixedDepositProductWritePlatformServiceJpaRepositoryImpl(context, fixedDepositProductRepository,
                fromApiJsonDataValidator, depositProductAssembler, accountMappingWritePlatformService, chartAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(GroupSavingsIndividualMonitoringWritePlatformService.class)
    public GroupSavingsIndividualMonitoringWritePlatformService groupSavingsIndividualMonitoringWritePlatformService(
            PlatformSecurityContext context, GSIMRepositoy gsimAccountRepository, LoanRepository loanRepository) {
        return new GroupSavingsIndividualMonitoringWritePlatformServiceImpl(context, gsimAccountRepository, loanRepository);
    }

    @Bean
    @ConditionalOnMissingBean(GSIMReadPlatformService.class)
    public GSIMReadPlatformService gsimReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            ColumnValidator columnValidator) {
        return new GSIMReadPlatformServiceImpl(jdbcTemplate, context, columnValidator);
    }

    @Bean
    @ConditionalOnMissingBean(RecurringDepositProductWritePlatformService.class)
    public RecurringDepositProductWritePlatformService recurringDepositProductWritePlatformService(PlatformSecurityContext context,
            RecurringDepositProductRepository recurringDepositProductRepository, DepositProductDataValidator fromApiJsonDataValidator,
            DepositProductAssembler depositProductAssembler,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService, InterestRateChartAssembler chartAssembler) {
        return new RecurringDepositProductWritePlatformServiceJpaRepositoryImpl(context, recurringDepositProductRepository,
                fromApiJsonDataValidator, depositProductAssembler, accountMappingWritePlatformService, chartAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountApplicationTransitionApiJsonValidator.class)
    public SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator(
            FromJsonHelper fromApiJsonHelper) {
        return new SavingsAccountApplicationTransitionApiJsonValidator(fromApiJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountChargeReadPlatformService.class)
    public SavingsAccountChargeReadPlatformService savingsAccountChargeReadPlatformService(PlatformSecurityContext context,
            ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, JdbcTemplate jdbcTemplate,
            DropdownReadPlatformService dropdownReadPlatformService, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new SavingsAccountChargeReadPlatformServiceImpl(context, chargeDropdownReadPlatformService, jdbcTemplate,
                dropdownReadPlatformService, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountInterestPostingService.class)
    public SavingsAccountInterestPostingService savingsAccountInterestPostingService(SavingsHelper savingsHelper) {
        return new SavingsAccountInterestPostingServiceImpl(savingsHelper);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountPostInterestService.class)
    public SavingsAccountPostInterestService savingsAccountPostInterestService(
            SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper) {
        return new SavingsAccountPostInterestServiceImpl(savingsAccountTransactionSummaryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountActivationService.class)
    public SavingsAccountActivationService savingsAccountActivationService(
            SavingsAccountPostInterestService savingsAccountPostInterestService) {
        return new SavingsAccountActivationServiceImpl(savingsAccountPostInterestService);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountReadPlatformService.class)
    public SavingsAccountReadPlatformService savingsAccountReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            SavingsAccountAssembler savingAccountAssembler, PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator,
            SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper, ColumnValidator columnValidator,
            SavingsAccountTransactionRepository savingsAccountTransactionRepository) {
        return new SavingsAccountReadPlatformServiceImpl(context, jdbcTemplate, savingAccountAssembler, paginationHelper, columnValidator,
                sqlGenerator, savingsAccountRepositoryWrapper, savingsAccountTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountTemplateReadPlatformService.class)
    public SavingsAccountTemplateReadPlatformService savingsAccountTemplateReadPlatformService(PlatformSecurityContext context,
            JdbcTemplate jdbcTemplate, ClientReadPlatformService clientReadPlatformService,
            GroupReadPlatformService groupReadPlatformService, SavingsProductReadPlatformService savingProductReadPlatformService,
            StaffReadService staffReadPlatformService, SavingsDropdownReadPlatformService dropdownReadPlatformService,
            ChargeReadPlatformService chargeReadPlatformService, EntityDatatableChecksReadService entityDatatableChecksReadService,
            ColumnValidator columnValidator) {
        return new SavingsAccountTemplateReadPlatformServiceImpl(context, jdbcTemplate, clientReadPlatformService, groupReadPlatformService,
                savingProductReadPlatformService, staffReadPlatformService, dropdownReadPlatformService, chargeReadPlatformService,
                entityDatatableChecksReadService, columnValidator);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsAccountWritePlatformService.class)
    public SavingsAccountWritePlatformService savingsAccountWritePlatformService(PlatformSecurityContext context,
            SavingsAccountDataValidator fromApiJsonDeserializer, SavingsAccountRepositoryWrapper savingAccountRepositoryWrapper,
            StaffRepositoryWrapper staffRepository, SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            SavingsAccountAssembler savingAccountAssembler, SavingsAccountTransactionDataValidator savingsAccountTransactionDataValidator,
            SavingsAccountChargeDataValidator savingsAccountChargeDataValidator,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService,
            JournalEntryWritePlatformService journalEntryWritePlatformService, SavingsAccountDomainService savingsAccountDomainService,
            NoteRepository noteRepository, AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService, ChargeRepositoryWrapper chargeRepository,
            SavingsAccountChargeRepositoryWrapper savingsAccountChargeRepository, HolidayRepositoryWrapper holidayRepository,
            WorkingDaysRepositoryWrapper workingDaysRepository, ConfigurationDomainService configurationDomainService,
            DepositAccountOnHoldTransactionRepository depositAccountOnHoldTransactionRepository,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService, AppUserRepositoryWrapper appuserRepository,
            StandingInstructionRepository standingInstructionRepository, BusinessEventNotifierService businessEventNotifierService,
            GSIMRepositoy gsimRepository, SavingsAccountInterestPostingService savingsAccountInterestPostingService,
            SavingsAccountPostInterestService savingsAccountPostInterestService,
            SavingsAccountActivationService savingsAccountActivationService, ExternalIdFactory externalIdFactory,
            ErrorHandler errorHandler) {
        return new SavingsAccountWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, savingAccountRepositoryWrapper,
                staffRepository, savingsAccountTransactionRepository, savingAccountAssembler, savingsAccountTransactionDataValidator,
                savingsAccountChargeDataValidator, paymentDetailWritePlatformService, journalEntryWritePlatformService,
                savingsAccountDomainService, noteRepository, accountTransfersReadPlatformService, accountAssociationsReadPlatformService,
                chargeRepository, savingsAccountChargeRepository, holidayRepository, workingDaysRepository, configurationDomainService,
                depositAccountOnHoldTransactionRepository, entityDatatableChecksWritePlatformService, appuserRepository,
                standingInstructionRepository, businessEventNotifierService, gsimRepository, savingsAccountInterestPostingService,
                savingsAccountPostInterestService, savingsAccountActivationService, externalIdFactory, errorHandler);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsApplicationProcessWritePlatformService.class)
    public SavingsApplicationProcessWritePlatformService savingsApplicationProcessWritePlatformService(PlatformSecurityContext context,
            SavingsAccountRepositoryWrapper savingAccountRepository, SavingsAccountAssembler savingAccountAssembler,
            SavingsAccountDataValidator savingsAccountDataValidator, AccountNumberGenerator accountNumberGenerator,
            ClientRepositoryWrapper clientRepository, GroupRepository groupRepository, SavingsProductRepository savingsProductRepository,
            NoteRepository noteRepository, StaffRepositoryWrapper staffRepository,
            SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator,
            SavingsAccountChargeAssembler savingsAccountChargeAssembler, CommandProcessingService commandProcessingService,
            SavingsAccountDomainService savingsAccountDomainService, SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, BusinessEventNotifierService businessEventNotifierService,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService, GSIMRepositoy gsimRepository,
            GroupRepositoryWrapper groupRepositoryWrapper, GroupSavingsIndividualMonitoringWritePlatformService gsimWritePlatformService) {
        return new SavingsApplicationProcessWritePlatformServiceJpaRepositoryImpl(context, savingAccountRepository, savingAccountAssembler,
                savingsAccountDataValidator, accountNumberGenerator, clientRepository, groupRepository, savingsProductRepository,
                noteRepository, staffRepository, savingsAccountApplicationTransitionApiJsonValidator, savingsAccountChargeAssembler,
                commandProcessingService, savingsAccountDomainService, savingsAccountWritePlatformService, accountNumberFormatRepository,
                businessEventNotifierService, entityDatatableChecksWritePlatformService, gsimRepository, groupRepositoryWrapper,
                gsimWritePlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsDropdownReadPlatformService.class)
    public SavingsDropdownReadPlatformService savingsDropdownReadPlatformService() {
        return new SavingsDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(SavingsProductReadPlatformService.class)
    public SavingsProductReadPlatformService savingsProductReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            CorevanceEntityAccessUtil corevanceEntityAccessUtil) {
        return new SavingsProductReadPlatformServiceImpl(context, jdbcTemplate, corevanceEntityAccessUtil);
    }

    @Bean
    @ConditionalOnMissingBean(SavingsProductWritePlatformService.class)
    public SavingsProductWritePlatformService savingsProductWritePlatformService(PlatformSecurityContext context,
            SavingsProductRepository savingProductRepository, SavingsProductDataValidator fromApiJsonDataValidator,
            SavingsProductAssembler savingsProductAssembler,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
            CorevanceEntityAccessUtil corevanceEntityAccessUtil) {
        return new SavingsProductWritePlatformServiceJpaRepositoryImpl(context, savingProductRepository, fromApiJsonDataValidator,
                savingsProductAssembler, accountMappingWritePlatformService, corevanceEntityAccessUtil);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean(SavingsSchedularInterestPoster.class)
    public SavingsSchedularInterestPoster savingsSchedularInterestPoster(
            SavingsAccountWritePlatformService savingsAccountWritePlatformService, JdbcTemplate jdbcTemplate,
            SavingsAccountReadPlatformService savingsAccountReadPlatformService, PlatformSecurityContext platformSecurityContext

    ) {
        return new SavingsSchedularInterestPoster(savingsAccountWritePlatformService, jdbcTemplate, savingsAccountReadPlatformService,
                platformSecurityContext);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean(SavingsSchedularInterestPosterTask.class)
    public SavingsSchedularInterestPosterTask savingsSchedularInterestPosterTask(SavingsSchedularInterestPoster interestPoster) {
        return new SavingsSchedularInterestPosterTask(interestPoster);
    }
}
