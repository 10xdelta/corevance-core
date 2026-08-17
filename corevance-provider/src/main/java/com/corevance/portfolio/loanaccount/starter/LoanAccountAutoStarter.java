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

import java.util.List;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.PhasedSettlementScheduleProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.CreocoreLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.EarlyPaymentLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.CorevanceStyleLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.HeavensFamilyLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.RBILoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.loanschedule.domain.ScheduledDateGenerator;
import com.corevance.portfolio.loanaccount.serialization.LoanChargeValidator;
import com.corevance.portfolio.loanaccount.service.LoanBalanceService;
import com.corevance.portfolio.loanaccount.service.LoanChargeService;
import com.corevance.portfolio.loanaccount.service.ProgressiveLoanInterestRefundServiceImpl;
import com.corevance.portfolio.loanaccount.service.schedule.LoanScheduleComponent;
import com.corevance.portfolio.loanproduct.calc.InstalmentCalculator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class LoanAccountAutoStarter {

    @Bean
    @Conditional(CreocoreLoanRepaymentScheduleTransactionProcessorCondition.class)
    public CreocoreLoanRepaymentScheduleTransactionProcessor creocoreLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new CreocoreLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(EarlyRepaymentLoanRepaymentScheduleTransactionProcessorCondition.class)
    public EarlyPaymentLoanRepaymentScheduleTransactionProcessor earlyPaymentLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new EarlyPaymentLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(MifosStandardLoanRepaymentScheduleTransactionProcessorCondition.class)
    public CorevanceStyleLoanRepaymentScheduleTransactionProcessor corevanceStyleLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new CorevanceStyleLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(HeavensFamilyLoanRepaymentScheduleTransactionProcessorCondition.class)
    public HeavensFamilyLoanRepaymentScheduleTransactionProcessor heavensFamilyLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new HeavensFamilyLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(InterestPrincipalPenaltiesFeesLoanRepaymentScheduleTransactionProcessorCondition.class)
    public InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor interestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new InterestPrincipalPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator,
                loanBalanceService);
    }

    @Bean
    @Conditional(PrincipalInterestPenaltiesFeesLoanRepaymentScheduleTransactionProcessorCondition.class)
    public PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor principalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator,
                loanBalanceService);
    }

    @Bean
    @Conditional(RBIIndiaLoanRepaymentScheduleTransactionProcessorCondition.class)
    public RBILoanRepaymentScheduleTransactionProcessor rbiLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new RBILoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Bean
    @Conditional(DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessorCondition.class)
    public DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor duePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator,
                loanBalanceService);
    }

    @Bean
    @Conditional(DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessorCondition.class)
    public DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor duePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor(
            final ExternalIdFactory externalIdFactory, final LoanChargeValidator loanChargeValidator,
            final LoanBalanceService loanBalanceService) {
        return new DuePenIntPriFeeInAdvancePenIntPriFeeLoanRepaymentScheduleTransactionProcessor(externalIdFactory, loanChargeValidator,
                loanBalanceService);
    }

    @Bean
    @ConditionalOnMissingBean(LoanRepaymentScheduleTransactionProcessorFactory.class)
    public LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory(
            PrincipalInterestPenaltyFeesOrderLoanRepaymentScheduleTransactionProcessor defaultLoanRepaymentScheduleTransactionProcessor,
            List<LoanRepaymentScheduleTransactionProcessor> processors) {
        return new LoanRepaymentScheduleTransactionProcessorFactory(defaultLoanRepaymentScheduleTransactionProcessor, processors);
    }

    @Bean
    @Conditional(PhasedSettlementScheduleProcessorCondition.class)
    public PhasedSettlementScheduleProcessor advancedPaymentScheduleTransactionProcessor(final InstalmentCalculator emiCalculator,
            final @Lazy ProgressiveLoanInterestRefundServiceImpl progressiveLoanInterestRefundService,
            final ExternalIdFactory externalIdFactory, final LoanScheduleComponent loanSchedule,
            final LoanChargeValidator loanChargeValidator, final LoanBalanceService loanBalanceService,
            @Lazy final LoanChargeService loanChargeService, final ScheduledDateGenerator scheduledDateGenerator,
            final ConfigurationDomainService configurationDomainService) {
        return new PhasedSettlementScheduleProcessor(emiCalculator, progressiveLoanInterestRefundService, externalIdFactory,
                loanSchedule, loanChargeValidator, loanBalanceService, loanChargeService, scheduledDateGenerator,
                configurationDomainService);
    }
}
