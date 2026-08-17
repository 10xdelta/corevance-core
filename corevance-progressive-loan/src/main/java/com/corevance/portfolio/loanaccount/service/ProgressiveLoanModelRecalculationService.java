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
package com.corevance.portfolio.loanaccount.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import com.corevance.portfolio.loanaccount.domain.ChangedTransactionDetail;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.PhasedSettlementScheduleProcessor;
import com.corevance.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProgressiveLoanModelRecalculationService {

    private final LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public ProgressiveLoanInterestScheduleModel getRecalculatedModel(Long loanId, LocalDate tillDate) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        LoanRepaymentScheduleTransactionProcessor transactionProcessor = transactionProcessorFactory
                .determineProcessor(loan.getTransactionProcessingStrategyCode());
        if (transactionProcessor instanceof PhasedSettlementScheduleProcessor advancedPaymentScheduleTransactionProcessor) {
            List<LoanTransaction> loanTransactions = loanTransactionRepository.findNonReversedTransactionsForReprocessingByLoan(loan);
            Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> result = advancedPaymentScheduleTransactionProcessor
                    .reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), tillDate, loanTransactions, loan.getCurrency(),
                            loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());
            return result.getRight();
        }
        return null;
    }

}
