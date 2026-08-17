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
package com.corevance.portfolio.loanaccount.starter;

import com.corevance.cob.service.LoanAccountLockService;
import com.corevance.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import com.corevance.infrastructure.codes.domain.CodeValueRepository;
import com.corevance.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import com.corevance.infrastructure.codes.service.CodeValueReadPlatformService;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.exception.ErrorHandler;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.infrastructure.core.service.PaginationHelper;
import com.corevance.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.infrastructure.security.utils.ColumnValidator;
import com.corevance.organisation.holiday.domain.HolidayRepository;
import com.corevance.organisation.holiday.domain.HolidayRepositoryWrapper;
import com.corevance.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import com.corevance.organisation.staff.domain.StaffRepository;
import com.corevance.organisation.staff.service.StaffReadService;
import com.corevance.organisation.teller.data.CashierTransactionDataValidator;
import com.corevance.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import com.corevance.portfolio.account.domain.AccountAssociationsRepository;
import com.corevance.portfolio.account.domain.AccountTransferDetailRepository;
import com.corevance.portfolio.account.service.AccountAssociationsReadPlatformService;
import com.corevance.portfolio.account.service.AccountNumberGenerator;
import com.corevance.portfolio.account.service.AccountTransfersReadPlatformService;
import com.corevance.portfolio.account.service.AccountTransfersWritePlatformService;
import com.corevance.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import com.corevance.portfolio.calendar.domain.CalendarInstanceRepository;
import com.corevance.portfolio.calendar.domain.CalendarRepository;
import com.corevance.portfolio.calendar.service.CalendarReadPlatformService;
import com.corevance.portfolio.charge.domain.ChargeRepositoryWrapper;
import com.corevance.portfolio.charge.service.ChargeDropdownReadPlatformService;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.portfolio.client.service.ClientReadPlatformService;
import com.corevance.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import com.corevance.portfolio.common.service.DropdownReadPlatformService;
import com.corevance.portfolio.delinquency.service.DelinquencyReadPlatformService;
import com.corevance.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import com.corevance.portfolio.fund.domain.FundRepository;
import com.corevance.portfolio.fund.service.FundReadPlatformService;
import com.corevance.portfolio.group.domain.GroupRepositoryWrapper;
import com.corevance.portfolio.group.service.GroupReadPlatformService;
import com.corevance.portfolio.interestpauses.service.InterestPauseReadPlatformService;
import com.corevance.portfolio.interestpauses.service.InterestPauseReadPlatformServiceImpl;
import com.corevance.portfolio.interestpauses.service.InterestPauseWritePlatformService;
import com.corevance.portfolio.interestpauses.service.InterestPauseWritePlatformServiceImpl;
import com.corevance.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import com.corevance.portfolio.loanaccount.domain.LoanAccountDomainService;
import com.corevance.portfolio.loanaccount.domain.LoanAccountService;
import com.corevance.portfolio.loanaccount.domain.LoanAmortizationAllocationMappingRepository;
import com.corevance.portfolio.loanaccount.domain.LoanChargeRepository;
import com.corevance.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import com.corevance.portfolio.loanaccount.domain.LoanRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import com.corevance.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import com.corevance.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import com.corevance.portfolio.loanaccount.mapper.LoanChargeMapper;
import com.corevance.portfolio.loanaccount.mapper.LoanCollateralManagementMapper;
import com.corevance.portfolio.loanaccount.mapper.LoanMapper;
import com.corevance.portfolio.loanaccount.mapper.LoanTransactionMapper;
import com.corevance.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import com.corevance.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import com.corevance.portfolio.loanaccount.rescheduleloan.domain.LoanTermVariationsRepository;
import com.corevance.portfolio.loanaccount.serialization.LoanApplicationTransitionValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanApplicationValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanChargeApiJsonValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanChargeValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanDisbursementValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanDownPaymentTransactionValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanForeclosureValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanOfficerValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanRefundValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanTransactionValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import com.corevance.portfolio.loanaccount.service.BulkLoansReadPlatformService;
import com.corevance.portfolio.loanaccount.service.BulkLoansReadPlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.BuyDownFeePlatformService;
import com.corevance.portfolio.loanaccount.service.BuyDownFeeReadPlatformService;
import com.corevance.portfolio.loanaccount.service.BuyDownFeeReadPlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.BuyDownFeeWritePlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import com.corevance.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformService;
import com.corevance.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.ILoanUtilService;
import com.corevance.portfolio.loanaccount.service.InterestRefundServiceDelegate;
import com.corevance.portfolio.loanaccount.service.LoanAccountServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanAccrualActivityProcessingService;
import com.corevance.portfolio.loanaccount.service.LoanAccrualEventService;
import com.corevance.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventService;
import com.corevance.portfolio.loanaccount.service.LoanAccrualTransactionBusinessEventServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import com.corevance.portfolio.loanaccount.service.LoanAmortizationAllocationService;
import com.corevance.portfolio.loanaccount.service.LoanAmortizationAllocationServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanApplicationWritePlatformService;
import com.corevance.portfolio.loanaccount.service.LoanApplicationWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.loanaccount.service.LoanArrearsAgingService;
import com.corevance.portfolio.loanaccount.service.LoanArrearsAgingServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanAssembler;
import com.corevance.portfolio.loanaccount.service.LoanAssemblerImpl;
import com.corevance.portfolio.loanaccount.service.LoanBalanceService;
import com.corevance.portfolio.loanaccount.service.LoanBuyDownFeeAmortizationEventService;
import com.corevance.portfolio.loanaccount.service.LoanBuyDownFeeAmortizationProcessingService;
import com.corevance.portfolio.loanaccount.service.LoanBuyDownFeeAmortizationProcessingServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanCalculateRepaymentPastDueService;
import com.corevance.portfolio.loanaccount.service.LoanCapitalizedIncomeAmortizationEventService;
import com.corevance.portfolio.loanaccount.service.LoanCapitalizedIncomeAmortizationProcessingService;
import com.corevance.portfolio.loanaccount.service.LoanCapitalizedIncomeAmortizationProcessingServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanChargeAssembler;
import com.corevance.portfolio.loanaccount.service.LoanChargePaidByReadService;
import com.corevance.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import com.corevance.portfolio.loanaccount.service.LoanChargeReadPlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanChargeService;
import com.corevance.portfolio.loanaccount.service.LoanChargeWritePlatformService;
import com.corevance.portfolio.loanaccount.service.LoanChargeWritePlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanDisbursementDetailsAssembler;
import com.corevance.portfolio.loanaccount.service.LoanDisbursementService;
import com.corevance.portfolio.loanaccount.service.LoanDownPaymentHandlerService;
import com.corevance.portfolio.loanaccount.service.LoanDownPaymentHandlerServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanJournalEntryPoster;
import com.corevance.portfolio.loanaccount.service.LoanMaximumAmountCalculator;
import com.corevance.portfolio.loanaccount.service.LoanOfficerService;
import com.corevance.portfolio.loanaccount.service.LoanOriginatorLinkingService;
import com.corevance.portfolio.loanaccount.service.LoanReadPlatformService;
import com.corevance.portfolio.loanaccount.service.LoanReadPlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanRefundService;
import com.corevance.portfolio.loanaccount.service.LoanRepaymentScheduleService;
import com.corevance.portfolio.loanaccount.service.LoanScheduleGeneratorService;
import com.corevance.portfolio.loanaccount.service.LoanScheduleService;
import com.corevance.portfolio.loanaccount.service.LoanStatusChangePlatformService;
import com.corevance.portfolio.loanaccount.service.LoanStatusChangePlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanTransactionAssembler;
import com.corevance.portfolio.loanaccount.service.LoanTransactionProcessingService;
import com.corevance.portfolio.loanaccount.service.LoanTransactionRelationReadService;
import com.corevance.portfolio.loanaccount.service.LoanTransactionService;
import com.corevance.portfolio.loanaccount.service.LoanUtilService;
import com.corevance.portfolio.loanaccount.service.LoanWritePlatformService;
import com.corevance.portfolio.loanaccount.service.LoanWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.loanaccount.service.ProgressiveLoanTransactionValidator;
import com.corevance.portfolio.loanaccount.service.ReplayedTransactionBusinessEventService;
import com.corevance.portfolio.loanaccount.service.ReplayedTransactionBusinessEventServiceImpl;
import com.corevance.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import com.corevance.portfolio.loanaccount.service.adjustment.LoanAdjustmentService;
import com.corevance.portfolio.loanaccount.service.schedule.LoanScheduleComponent;
import com.corevance.portfolio.loanproduct.domain.LoanProductRepository;
import com.corevance.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import com.corevance.portfolio.loanproduct.service.LoanProductReadPlatformService;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import com.corevance.portfolio.paymenttype.service.PaymentTypeReadService;
import com.corevance.portfolio.rate.service.RateAssembler;
import com.corevance.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import com.corevance.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import com.corevance.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import com.corevance.portfolio.savings.service.GSIMReadPlatformService;
import com.corevance.portfolio.tax.service.ChargeTaxApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class LoanAccountConfiguration {

    @Bean
    @ConditionalOnMissingBean(BulkLoansReadPlatformService.class)
    public BulkLoansReadPlatformService bulkLoansReadPlatformServicev(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            AccountDetailsReadPlatformService accountDetailsReadPlatformService) {
        return new BulkLoansReadPlatformServiceImpl(jdbcTemplate, context, accountDetailsReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(GLIMAccountInfoReadPlatformService.class)
    public GLIMAccountInfoReadPlatformService glimAccountInfoReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            AccountDetailsReadPlatformService accountDetailsReadPlatforService) {
        return new GLIMAccountInfoReadPlatformServiceImpl(jdbcTemplate, context, accountDetailsReadPlatforService);
    }

    @Bean
    @ConditionalOnMissingBean(GLIMAccountInfoWritePlatformService.class)
    public GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService(GLIMAccountInfoRepository glimAccountRepository) {
        return new GLIMAccountInfoWritePlatformServiceImpl(glimAccountRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccrualTransactionBusinessEventService.class)
    public LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService(
            final BusinessEventNotifierService businessEventNotifierService, final LoanTransactionRepository loanTransactionRepository) {
        return new LoanAccrualTransactionBusinessEventServiceImpl(businessEventNotifierService, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanApplicationWritePlatformService.class)
    public LoanApplicationWritePlatformService loanApplicationWritePlatformService(PlatformSecurityContext context,
            LoanApplicationTransitionValidator loanApplicationTransitionValidator, LoanApplicationValidator loanApplicationValidator,
            LoanRepositoryWrapper loanRepositoryWrapper, NoteRepository noteRepository, LoanAssembler loanAssembler,
            CalendarRepository calendarRepository, CalendarInstanceRepository calendarInstanceRepository,
            SavingsAccountRepositoryWrapper savingsAccountRepository, AccountAssociationsRepository accountAssociationsRepository,
            BusinessEventNotifierService businessEventNotifierService, LoanScheduleAssembler loanScheduleAssembler,
            LoanUtilService loanUtilService, CalendarReadPlatformService calendarReadPlatformService,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService, GLIMAccountInfoRepository glimRepository,
            LoanRepository loanRepository, GSIMReadPlatformService gsimReadPlatformService,
            LoanLifecycleStateMachine loanLifecycleStateMachine, LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator, LoanScheduleService loanScheduleService,
            LoanOriginatorLinkingService loanOriginatorLinkingService) {
        return new LoanApplicationWritePlatformServiceJpaRepositoryImpl(context, loanApplicationTransitionValidator,
                loanApplicationValidator, loanRepositoryWrapper, noteRepository, loanAssembler, calendarRepository,
                calendarInstanceRepository, savingsAccountRepository, accountAssociationsRepository, businessEventNotifierService,
                loanScheduleAssembler, loanUtilService, calendarReadPlatformService, entityDatatableChecksWritePlatformService,
                glimRepository, loanRepository, gsimReadPlatformService, loanLifecycleStateMachine, loanAccrualsProcessingService,
                loanDownPaymentTransactionValidator, loanScheduleService, loanOriginatorLinkingService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanArrearsAgingService.class)
    public LoanArrearsAgingService loanArrearsAgingService(JdbcTemplate jdbcTemplate,
            BusinessEventNotifierService businessEventNotifierService, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new LoanArrearsAgingServiceImpl(jdbcTemplate, businessEventNotifierService, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAssembler.class)
    public LoanAssembler loanAssembler(FromJsonHelper fromApiJsonHelper, LoanRepositoryWrapper loanRepository,
            LoanProductRepository loanProductRepository, ClientRepositoryWrapper clientRepository, GroupRepositoryWrapper groupRepository,
            FundRepository fundRepository, StaffRepository staffRepository, CodeValueRepositoryWrapper codeValueRepository,
            LoanScheduleAssembler loanScheduleAssembler, LoanChargeAssembler loanChargeAssembler,
            LoanCollateralAssembler collateralAssembler,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            HolidayRepository holidayRepository, ConfigurationDomainService configurationDomainService,
            WorkingDaysRepositoryWrapper workingDaysRepository, RateAssembler rateAssembler, ExternalIdFactory externalIdFactory,
            AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, GLIMAccountInfoRepository glimRepository,
            AccountNumberGenerator accountNumberGenerator, GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService,
            LoanCollateralAssembler loanCollateralAssembler, LoanDisbursementDetailsAssembler loanDisbursementDetailsAssembler,
            LoanChargeMapper loanChargeMapper, LoanCollateralManagementMapper loanCollateralManagementMapper,
            LoanAccrualsProcessingService loanAccrualsProcessingService, LoanDisbursementService loanDisbursementService,
            LoanChargeService loanChargeService, LoanOfficerService loanOfficerService, LoanScheduleComponent loanSchedule,
            LoanScheduleService loanScheduleService, LoanUtilService loanUtilService) {
        return new LoanAssemblerImpl(fromApiJsonHelper, loanRepository, loanProductRepository, clientRepository, groupRepository,
                fundRepository, staffRepository, codeValueRepository, loanScheduleAssembler, loanChargeAssembler, collateralAssembler,
                loanRepaymentScheduleTransactionProcessorFactory, holidayRepository, configurationDomainService, workingDaysRepository,
                rateAssembler, externalIdFactory, accountNumberFormatRepository, glimRepository, accountNumberGenerator,
                glimAccountInfoWritePlatformService, loanCollateralAssembler, loanDisbursementDetailsAssembler, loanChargeMapper,
                loanCollateralManagementMapper, loanAccrualsProcessingService, loanDisbursementService, loanChargeService,
                loanOfficerService, loanSchedule, loanScheduleService, loanUtilService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanTransactionAssembler.class)
    public LoanTransactionAssembler loanTransactionAssembler(ExternalIdFactory externalIdFactory,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService) {

        return new LoanTransactionAssembler(externalIdFactory, paymentDetailWritePlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCalculateRepaymentPastDueService.class)
    public LoanCalculateRepaymentPastDueService loanCalculateRepaymentPastDueService() {
        return new LoanCalculateRepaymentPastDueService();
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeAssembler.class)
    public LoanChargeAssembler loanChargeAssembler(final FromJsonHelper fromApiJsonHelper, final ChargeRepositoryWrapper chargeRepository,
            final LoanChargeRepository loanChargeRepository, final LoanProductRepository loanProductRepository,
            final ExternalIdFactory externalIdFactory, final LoanChargeService loanChargeService) {
        return new LoanChargeAssembler(fromApiJsonHelper, chargeRepository, loanChargeRepository, loanProductRepository, externalIdFactory,
                loanChargeService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeReadPlatformService.class)
    public LoanChargeReadPlatformService loanChargeReadPlatformService(JdbcTemplate jdbcTemplate,
            ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, DropdownReadPlatformService dropdownReadPlatformService,
            LoanChargeRepository loanChargeRepository) {
        return new LoanChargeReadPlatformServiceImpl(jdbcTemplate, chargeDropdownReadPlatformService, dropdownReadPlatformService,
                loanChargeRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeWritePlatformService.class)
    public LoanChargeWritePlatformService loanChargeWritePlatformService(LoanChargeApiJsonValidator loanChargeApiJsonValidator,
            LoanAssembler loanAssembler, ChargeRepositoryWrapper chargeRepository,
            BusinessEventNotifierService businessEventNotifierService, LoanTransactionRepository loanTransactionRepository,
            AccountTransfersWritePlatformService accountTransfersWritePlatformService, LoanRepositoryWrapper loanRepositoryWrapper,
            LoanAccountDomainService loanAccountDomainService, LoanChargeRepository loanChargeRepository,
            LoanWritePlatformService loanWritePlatformService, LoanUtilService loanUtilService,
            LoanChargeReadPlatformService loanChargeReadPlatformService, LoanLifecycleStateMachine loanLifecycleStateMachine,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService, FromJsonHelper fromApiJsonHelper,
            ConfigurationDomainService configurationDomainService, ExternalIdFactory externalIdFactory,
            AccountTransferDetailRepository accountTransferDetailRepository, LoanChargeAssembler loanChargeAssembler,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService, NoteRepository noteRepository,
            LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator, LoanChargeValidator loanChargeValidator,
            LoanScheduleService loanScheduleService, ReprocessLoanTransactionsService reprocessLoanTransactionsService,
            LoanAccountService loanAccountService, LoanAdjustmentService loanAdjustmentService, LoanChargeService loanChargeService,
            LoanJournalEntryPoster loanJournalEntryPoster) {
        return new LoanChargeWritePlatformServiceImpl(loanChargeApiJsonValidator, loanAssembler, chargeRepository,
                businessEventNotifierService, loanTransactionRepository, accountTransfersWritePlatformService, loanRepositoryWrapper,
                loanAccountDomainService, loanChargeRepository, loanWritePlatformService, loanUtilService, loanChargeReadPlatformService,
                loanLifecycleStateMachine, accountAssociationsReadPlatformService, fromApiJsonHelper, configurationDomainService,
                externalIdFactory, accountTransferDetailRepository, loanChargeAssembler, paymentDetailWritePlatformService, noteRepository,
                loanAccrualsProcessingService, loanDownPaymentTransactionValidator, loanChargeValidator, loanScheduleService,
                reprocessLoanTransactionsService, loanAccountService, loanAdjustmentService, loanChargeService, loanJournalEntryPoster);
    }

    @Bean
    @ConditionalOnMissingBean(LoanReadPlatformService.class)
    public LoanReadPlatformServiceImpl loanReadPlatformService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context,
            LoanRepositoryWrapper loanRepositoryWrapper, ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            LoanProductReadPlatformService loanProductReadPlatformService, ClientReadPlatformService clientReadPlatformService,
            GroupReadPlatformService groupReadPlatformService, LoanDropdownReadPlatformService loanDropdownReadPlatformService,
            FundReadPlatformService fundReadPlatformService, ChargeReadPlatformService chargeReadPlatformService,
            CodeValueReadPlatformService codeValueReadPlatformService, CalendarReadPlatformService calendarReadPlatformService,
            StaffReadService staffReadPlatformService, PaginationHelper paginationHelper,
            PaymentTypeReadService paymentTypeReadPlatformService, FloatingRatesReadPlatformService floatingRatesReadPlatformService,
            LoanUtilService loanUtilService, ConfigurationDomainService configurationDomainService,
            AccountDetailsReadPlatformService accountDetailsReadPlatformService, ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, DelinquencyReadPlatformService delinquencyReadPlatformService,
            LoanTransactionRepository loanTransactionRepository, LoanChargePaidByReadService loanChargePaidByReadService,
            LoanTransactionRelationReadService loanTransactionRelationReadService, LoanForeclosureValidator loanForeclosureValidator,
            LoanTransactionMapper loanTransactionMapper, LoanTransactionProcessingService loanTransactionProcessingService,
            LoanBalanceService loanBalanceService, LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository,
            LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository,
            @Lazy InterestRefundServiceDelegate interestRefundServiceDelegate, LoanMaximumAmountCalculator loanMaximumAmountCalculator,
            LoanRepaymentScheduleService loanRepaymentScheduleService) {
        return new LoanReadPlatformServiceImpl(jdbcTemplate, context, loanRepositoryWrapper, applicationCurrencyRepository,
                loanProductReadPlatformService, clientReadPlatformService, groupReadPlatformService, loanDropdownReadPlatformService,
                fundReadPlatformService, chargeReadPlatformService, codeValueReadPlatformService, calendarReadPlatformService,
                staffReadPlatformService, paginationHelper, paymentTypeReadPlatformService, floatingRatesReadPlatformService,
                loanUtilService, configurationDomainService, accountDetailsReadPlatformService, columnValidator, sqlGenerator,
                delinquencyReadPlatformService, loanTransactionRepository, loanChargePaidByReadService, loanTransactionRelationReadService,
                loanForeclosureValidator, loanTransactionMapper, loanTransactionProcessingService, loanBalanceService,
                loanCapitalizedIncomeBalanceRepository, loanBuyDownFeeBalanceRepository, interestRefundServiceDelegate,
                loanMaximumAmountCalculator, loanRepaymentScheduleService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanStatusChangePlatformService.class)
    public LoanStatusChangePlatformService loanStatusChangePlatformService(BusinessEventNotifierService businessEventNotifierService,
            LoanAccrualActivityProcessingService loanAccrualActivityProcessingService) {
        return new LoanStatusChangePlatformServiceImpl(businessEventNotifierService, loanAccrualActivityProcessingService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccrualEventService.class)
    public LoanAccrualEventService loanAccrualEventService(BusinessEventNotifierService businessEventNotifierService,
            LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanAccrualActivityProcessingService loanAccrualActivityProcessingService) {
        return new LoanAccrualEventService(businessEventNotifierService, loanAccrualsProcessingService,
                loanAccrualActivityProcessingService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanUtilService.class)
    public LoanUtilService loanUtilService(ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            CalendarInstanceRepository calendarInstanceRepository, ConfigurationDomainService configurationDomainService,
            HolidayRepository holidayRepository, WorkingDaysRepositoryWrapper workingDaysRepository,
            LoanScheduleGeneratorFactory loanScheduleFactory, FloatingRatesReadPlatformService floatingRatesReadPlatformService,
            CalendarReadPlatformService calendarReadPlatformService, NoteRepository noteRepository) {
        return new LoanUtilService(applicationCurrencyRepository, calendarInstanceRepository, configurationDomainService, holidayRepository,
                workingDaysRepository, loanScheduleFactory, floatingRatesReadPlatformService, calendarReadPlatformService, noteRepository);
    }

    @Bean
    @ConditionalOnMissingBean(BuyDownFeePlatformService.class)
    public BuyDownFeePlatformService buyDownFeePlatformService(ProgressiveLoanTransactionValidator loanTransactionValidator,
            LoanAssembler loanAssembler, LoanTransactionRepository loanTransactionRepository,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService, LoanJournalEntryPoster loanJournalEntryPoster,
            ExternalIdFactory externalIdFactory, LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository,
            BusinessEventNotifierService businessEventNotifierService, CodeValueRepository codeValueRepository,
            TransactionBoundApplicationEventPublisher eventPublisher) {
        return new BuyDownFeeWritePlatformServiceImpl(loanTransactionValidator, loanAssembler, loanTransactionRepository,
                paymentDetailWritePlatformService, loanJournalEntryPoster, externalIdFactory, loanBuyDownFeeBalanceRepository,
                businessEventNotifierService, codeValueRepository, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(BuyDownFeeReadPlatformService.class)
    public BuyDownFeeReadPlatformService buyDownFeeReadPlatformService(
            final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository, final LoanRepository loanRepository) {
        return new BuyDownFeeReadPlatformServiceImpl(loanBuyDownFeeBalanceRepository, loanRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanWritePlatformService.class)
    public LoanWritePlatformService loanWritePlatformService(PlatformSecurityContext context,
            LoanTransactionValidator loanTransactionValidator,
            LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer, LoanRepositoryWrapper loanRepositoryWrapper,
            LoanAccountDomainService loanAccountDomainService, NoteRepository noteRepository,
            LoanTransactionRepository loanTransactionRepository, LoanTransactionRelationRepository loanTransactionRelationRepository,
            LoanAssembler loanAssembler, CalendarInstanceRepository calendarInstanceRepository,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService, HolidayRepositoryWrapper holidayRepository,
            ConfigurationDomainService configurationDomainService, WorkingDaysRepositoryWrapper workingDaysRepository,
            AccountTransfersWritePlatformService accountTransfersWritePlatformService,
            AccountTransfersReadPlatformService accountTransfersReadPlatformService,
            AccountAssociationsReadPlatformService accountAssociationsReadPlatformService, LoanReadPlatformService loanReadPlatformService,
            FromJsonHelper fromApiJsonHelper, CalendarRepository calendarRepository,
            LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService,
            LoanApplicationValidator loanApplicationValidator, AccountAssociationsRepository accountAssociationRepository,
            AccountTransferDetailRepository accountTransferDetailRepository, BusinessEventNotifierService businessEventNotifierService,
            GuarantorDomainService guarantorDomainService, LoanUtilService loanUtilService,
            EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService,
            CodeValueRepositoryWrapper codeValueRepository, CashierTransactionDataValidator cashierTransactionDataValidator,
            GLIMAccountInfoRepository glimRepository, LoanRepository loanRepository,
            RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler,
            PostDatedChecksRepository postDatedChecksRepository,
            LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository,
            LoanLifecycleStateMachine loanLifecycleStateMachine, LoanAccountLockService loanAccountLockService,
            ExternalIdFactory externalIdFactory, LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService,
            ErrorHandler errorHandler, LoanDownPaymentHandlerService loanDownPaymentHandlerService,
            LoanTransactionAssembler loanTransactionAssembler, LoanAccrualsProcessingService loanAccrualsProcessingService,
            LoanOfficerValidator loanOfficerValidator, LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator,
            LoanDisbursementService loanDisbursementService, LoanScheduleService loanScheduleService,
            LoanChargeValidator loanChargeValidator, LoanOfficerService loanOfficerService,
            ReprocessLoanTransactionsService reprocessLoanTransactionsService, LoanAccountService loanAccountService,
            LoanJournalEntryPoster journalEntryPoster, LoanAdjustmentService loanAdjustmentService, LoanMapper loanMapper,
            LoanTransactionProcessingService loanTransactionProcessingService, final LoanBalanceService loanBalanceService,
            LoanTransactionService loanTransactionService) {
        return new LoanWritePlatformServiceJpaRepositoryImpl(context, loanTransactionValidator, loanUpdateCommandFromApiJsonDeserializer,
                loanRepositoryWrapper, loanAccountDomainService, noteRepository, loanTransactionRepository,
                loanTransactionRelationRepository, loanAssembler, calendarInstanceRepository, paymentDetailWritePlatformService,
                holidayRepository, configurationDomainService, workingDaysRepository, accountTransfersWritePlatformService,
                accountTransfersReadPlatformService, accountAssociationsReadPlatformService, loanReadPlatformService, fromApiJsonHelper,
                calendarRepository, loanScheduleHistoryWritePlatformService, loanApplicationValidator, accountAssociationRepository,
                accountTransferDetailRepository, businessEventNotifierService, guarantorDomainService, loanUtilService,
                entityDatatableChecksWritePlatformService, codeValueRepository, cashierTransactionDataValidator, glimRepository,
                loanRepository, repaymentWithPostDatedChecksAssembler, postDatedChecksRepository,
                loanRepaymentScheduleInstallmentRepository, loanLifecycleStateMachine, loanAccountLockService, externalIdFactory,
                loanAccrualTransactionBusinessEventService, errorHandler, loanDownPaymentHandlerService, loanTransactionAssembler,
                loanAccrualsProcessingService, loanOfficerValidator, loanDownPaymentTransactionValidator, loanDisbursementService,
                loanScheduleService, loanChargeValidator, loanOfficerService, reprocessLoanTransactionsService, loanAccountService,
                journalEntryPoster, loanAdjustmentService, loanMapper, loanTransactionProcessingService, loanBalanceService,
                loanTransactionService);
    }

    @Bean
    @ConditionalOnMissingBean(ReplayedTransactionBusinessEventService.class)
    public ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService(
            BusinessEventNotifierService businessEventNotifierService, LoanTransactionRepository loanTransactionRepository) {
        return new ReplayedTransactionBusinessEventServiceImpl(businessEventNotifierService, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDownPaymentHandlerService.class)
    public LoanDownPaymentHandlerService loanDownPaymentHandlerService(LoanTransactionRepository loanTransactionRepository,
            BusinessEventNotifierService businessEventNotifierService,
            LoanDownPaymentTransactionValidator loanDownPaymentTransactionValidator, LoanScheduleService loanScheduleService,
            LoanRefundService loanRefundService, LoanRefundValidator loanRefundValidator,
            ReprocessLoanTransactionsService reprocessLoanTransactionsService,
            LoanTransactionProcessingService loanTransactionProcessingService, LoanLifecycleStateMachine loanLifecycleStateMachine,
            LoanBalanceService loanBalanceService, LoanTransactionService loanTransactionService,
            LoanJournalEntryPoster journalEntryPoster) {
        return new LoanDownPaymentHandlerServiceImpl(loanTransactionRepository, businessEventNotifierService,
                loanDownPaymentTransactionValidator, loanScheduleService, loanRefundService, loanRefundValidator,
                reprocessLoanTransactionsService, loanTransactionProcessingService, loanLifecycleStateMachine, loanBalanceService,
                loanTransactionService, journalEntryPoster);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDisbursementDetailsAssembler.class)
    public LoanDisbursementDetailsAssembler loanDisbursementDetailsAssembler(FromJsonHelper fromApiJsonHelper) {
        return new LoanDisbursementDetailsAssembler(fromApiJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDisbursementService.class)
    public LoanDisbursementService loanDisbursementService(LoanChargeValidator loanChargeValidator,
            LoanDisbursementValidator loanDisbursementValidator, LoanChargeService loanChargeService, LoanBalanceService loanBalanceService,
            LoanJournalEntryPoster journalEntryPoster, LoanTransactionRepository loanTransactionRepository,
            ConfigurationDomainService configurationDomainService) {
        return new LoanDisbursementService(loanChargeValidator, loanDisbursementValidator, loanChargeService, loanBalanceService,
                journalEntryPoster, loanTransactionRepository, configurationDomainService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanChargeService.class)
    public LoanChargeService loanChargeService(final LoanChargeValidator loanChargeValidator,
            final LoanTransactionProcessingService loanTransactionProcessingService,
            final LoanLifecycleStateMachine loanLifecycleStateMachine, final LoanBalanceService loanBalanceService,
            final LoanScheduleGeneratorService loanScheduleGeneratorService,
            final ChargeTaxApplicationService chargeTaxApplicationService) {
        return new LoanChargeService(loanChargeValidator, loanTransactionProcessingService, loanLifecycleStateMachine, loanBalanceService,
                loanScheduleGeneratorService, chargeTaxApplicationService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanScheduleService.class)
    public LoanScheduleService loanScheduleService(final LoanChargeService loanChargeService,
            final ReprocessLoanTransactionsService reprocessLoanTransactionsService, final LoanMapper loanMapper,
            final LoanTransactionProcessingService loanTransactionProcessingService, LoanScheduleComponent loanSchedule,
            final LoanTransactionRepository loanTransactionRepository, final ILoanUtilService loanUtilService) {
        return new LoanScheduleService(loanChargeService, reprocessLoanTransactionsService, loanMapper, loanTransactionProcessingService,
                loanSchedule, loanTransactionRepository, loanUtilService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanOfficerService.class)
    public LoanOfficerService loanOfficerService(LoanOfficerValidator loanOfficerValidator) {
        return new LoanOfficerService(loanOfficerValidator);
    }

    @Bean
    @ConditionalOnMissingBean(LoanRefundService.class)
    public LoanRefundService loanRefundService(final LoanRefundValidator loanRefundValidator,
            final LoanTransactionProcessingService loanTransactionProcessingService,
            final LoanLifecycleStateMachine loanLifecycleStateMachine) {
        return new LoanRefundService(loanRefundValidator, loanTransactionProcessingService, loanLifecycleStateMachine);
    }

    @Bean
    @ConditionalOnMissingBean(InterestPauseReadPlatformService.class)
    public InterestPauseReadPlatformService interestPauseReadPlatformService(LoanTermVariationsRepository loanTermVariationsRepository) {
        return new InterestPauseReadPlatformServiceImpl(loanTermVariationsRepository);
    }

    @Bean
    @ConditionalOnMissingBean(InterestPauseWritePlatformService.class)
    public InterestPauseWritePlatformService interestPauseWritePlatformService(LoanTermVariationsRepository loanTermVariationsRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, LoanAssembler loanAssembler,
            BusinessEventNotifierService businessEventNotifierService, LoanScheduleService loanScheduleService) {
        return new InterestPauseWritePlatformServiceImpl(loanTermVariationsRepository, loanRepositoryWrapper, loanAssembler,
                businessEventNotifierService, loanScheduleService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAccountService.class)
    public LoanAccountService loanAccountService(LoanRepositoryWrapper loanRepositoryWrapper,
            LoanTransactionRepository loanTransactionRepository) {
        return new LoanAccountServiceImpl(loanRepositoryWrapper, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCapitalizedIncomeAmortizationEventService.class)
    public LoanCapitalizedIncomeAmortizationEventService loanCapitalizedIncomeAmortizationEventService(
            BusinessEventNotifierService businessEventNotifierService,
            LoanCapitalizedIncomeAmortizationProcessingService loanCapitalizedIncomeAmortizationProcessingService) {
        return new LoanCapitalizedIncomeAmortizationEventService(businessEventNotifierService,
                loanCapitalizedIncomeAmortizationProcessingService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCapitalizedIncomeAmortizationProcessingService.class)
    public LoanCapitalizedIncomeAmortizationProcessingService loanCapitalizedIncomeAmortizationProcessingService(
            final ConfigurationDomainService configurationDomainService, final LoanTransactionRepository loanTransactionRepository,
            final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository,
            final BusinessEventNotifierService businessEventNotifierService, final LoanJournalEntryPoster journalEntryPoster,
            final ExternalIdFactory externalIdFactory, final LoanAmortizationAllocationService loanAmortizationAllocationService) {
        return new LoanCapitalizedIncomeAmortizationProcessingServiceImpl(configurationDomainService, loanTransactionRepository,
                loanCapitalizedIncomeBalanceRepository, businessEventNotifierService, journalEntryPoster, externalIdFactory,
                loanAmortizationAllocationService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanBuyDownFeeAmortizationProcessingService.class)
    public LoanBuyDownFeeAmortizationProcessingService loanBuyDownFeeAmortizationProcessingService(
            final LoanTransactionRepository loanTransactionRepository,
            final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository,
            final BusinessEventNotifierService businessEventNotifierService, final LoanJournalEntryPoster journalEntryPoster,
            final ExternalIdFactory externalIdFactory, final LoanAmortizationAllocationService loanAmortizationAllocationService) {
        return new LoanBuyDownFeeAmortizationProcessingServiceImpl(loanTransactionRepository, loanBuyDownFeeBalanceRepository,
                businessEventNotifierService, journalEntryPoster, externalIdFactory, loanAmortizationAllocationService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanBuyDownFeeAmortizationEventService.class)
    public LoanBuyDownFeeAmortizationEventService loanBuyDownFeeAmortizationEventService(
            BusinessEventNotifierService businessEventNotifierService,
            LoanBuyDownFeeAmortizationProcessingService loanBuyDownFeeAmortizationProcessingService) {
        return new LoanBuyDownFeeAmortizationEventService(businessEventNotifierService, loanBuyDownFeeAmortizationProcessingService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanAmortizationAllocationService.class)
    public LoanAmortizationAllocationService loanAmortizationAllocationService(
            final LoanAmortizationAllocationMappingRepository loanAmortizationAllocationMappingRepository,
            final LoanTransactionRepository loanTransactionRepository,
            final LoanCapitalizedIncomeBalanceRepository capitalizedIncomeBalanceRepository,
            final LoanBuyDownFeeBalanceRepository buyDownFeeBalanceRepository) {
        return new LoanAmortizationAllocationServiceImpl(loanAmortizationAllocationMappingRepository, loanTransactionRepository,
                capitalizedIncomeBalanceRepository, buyDownFeeBalanceRepository);
    }
}
