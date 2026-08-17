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

import com.corevance.infrastructure.codes.domain.CodeValueRepository;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import com.corevance.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import com.corevance.portfolio.loanaccount.serialization.LoanTransactionValidator;
import com.corevance.portfolio.loanaccount.service.CapitalizedIncomeBalanceReadService;
import com.corevance.portfolio.loanaccount.service.CapitalizedIncomeBalanceReadServiceImpl;
import com.corevance.portfolio.loanaccount.service.CapitalizedIncomeBalanceService;
import com.corevance.portfolio.loanaccount.service.CapitalizedIncomeBalanceServiceImpl;
import com.corevance.portfolio.loanaccount.service.CapitalizedIncomePlatformService;
import com.corevance.portfolio.loanaccount.service.CapitalizedIncomeWritePlatformServiceImpl;
import com.corevance.portfolio.loanaccount.service.LoanAssembler;
import com.corevance.portfolio.loanaccount.service.LoanBalanceService;
import com.corevance.portfolio.loanaccount.service.LoanJournalEntryPoster;
import com.corevance.portfolio.loanaccount.service.LoanMaximumAmountCalculator;
import com.corevance.portfolio.loanaccount.service.LoanScheduleService;
import com.corevance.portfolio.loanaccount.service.ProgressiveLoanTransactionValidator;
import com.corevance.portfolio.loanaccount.service.ProgressiveLoanTransactionValidatorImpl;
import com.corevance.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import com.corevance.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProgressiveLoanAccountConfiguration {

    @Bean
    @ConditionalOnMissingBean(CapitalizedIncomePlatformService.class)
    public CapitalizedIncomePlatformService capitalizedIncomePlatformService(ProgressiveLoanTransactionValidator loanTransactionValidator,
            LoanAssembler loanAssembler, LoanTransactionRepository loanTransactionRepository,
            PaymentDetailWritePlatformService paymentDetailWritePlatformService, LoanJournalEntryPoster journalEntryPoster,
            ExternalIdFactory externalIdFactory, LoanCapitalizedIncomeBalanceRepository capitalizedIncomeBalanceRepository,
            ReprocessLoanTransactionsService reprocessLoanTransactionsService, LoanBalanceService loanBalanceService,
            LoanLifecycleStateMachine loanLifecycleStateMachine, BusinessEventNotifierService businessEventNotifierService,
            CodeValueRepository codeValueRepository, LoanScheduleService loanScheduleService,
            TransactionBoundApplicationEventPublisher eventPublisher) {
        return new CapitalizedIncomeWritePlatformServiceImpl(loanTransactionValidator, loanAssembler, loanTransactionRepository,
                paymentDetailWritePlatformService, journalEntryPoster, externalIdFactory, capitalizedIncomeBalanceRepository,
                reprocessLoanTransactionsService, loanBalanceService, loanLifecycleStateMachine, businessEventNotifierService,
                codeValueRepository, loanScheduleService, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(ProgressiveLoanTransactionValidator.class)
    public ProgressiveLoanTransactionValidator progressiveLoanTransactionValidator(FromJsonHelper fromApiJsonHelper,
            LoanTransactionValidator loanTransactionValidator, LoanRepositoryWrapper loanRepositoryWrapper,
            LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository,
            LoanBuyDownFeeBalanceRepository loanBuydownFeeBalanceRepository, LoanTransactionRepository loanTransactionRepository,
            LoanMaximumAmountCalculator loanMaximumAmountCalculator) {
        return new ProgressiveLoanTransactionValidatorImpl(fromApiJsonHelper, loanTransactionValidator, loanRepositoryWrapper,
                loanCapitalizedIncomeBalanceRepository, loanBuydownFeeBalanceRepository, loanTransactionRepository,
                loanMaximumAmountCalculator);
    }

    @Bean
    @ConditionalOnMissingBean(CapitalizedIncomeBalanceService.class)
    public CapitalizedIncomeBalanceService capitalizedIncomeBalanceService(
            LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository) {
        return new CapitalizedIncomeBalanceServiceImpl(loanCapitalizedIncomeBalanceRepository);
    }

    @Bean
    @ConditionalOnMissingBean(CapitalizedIncomeBalanceReadService.class)
    public CapitalizedIncomeBalanceReadService capitalizedIncomeBalanceReadService(LoanRepositoryWrapper loanRepository,
            LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository) {
        return new CapitalizedIncomeBalanceReadServiceImpl(loanRepository, loanCapitalizedIncomeBalanceRepository);
    }

}
