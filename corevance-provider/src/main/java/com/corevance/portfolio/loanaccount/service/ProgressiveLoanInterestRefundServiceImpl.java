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

import static com.corevance.portfolio.loanaccount.domain.LoanTransactionType.REPAYMENT;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import com.corevance.infrastructure.core.service.MathUtil;
import com.corevance.organisation.monetary.domain.Money;
import com.corevance.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import com.corevance.portfolio.loanaccount.data.TransactionChangeData;
import com.corevance.portfolio.loanaccount.domain.ChangedTransactionDetail;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanTermVariations;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionType;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.PhasedSettlementScheduleProcessor;
import com.corevance.portfolio.loanaccount.starter.PhasedSettlementScheduleProcessorCondition;
import com.corevance.portfolio.loanproduct.calc.InstalmentCalculator;
import com.corevance.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Conditional(PhasedSettlementScheduleProcessorCondition.class)
@Service
public class ProgressiveLoanInterestRefundServiceImpl implements InterestRefundService {

    private final InstalmentCalculator emiCalculator;
    private final LoanAssembler loanAssembler;
    private final LoanScheduleService loanScheduleService;
    private final LoanUtilService loanUtilService;
    private final LoanTransactionProcessingService loanTransactionProcessingService;

    private static void simulateRepaymentForDisbursements(LoanTransaction lt, final AtomicReference<BigDecimal> refundFinal,
            List<LoanTransaction> collect) {
        LoanTransaction copy = new LoanTransaction(lt.getLoan(), lt.getLoan().getOffice(), lt.getTypeOf(), lt.getDateOf(), lt.getAmount(),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, null);
        if (LoanTransactionType.CHARGE_PAYMENT.equals(copy.getTypeOf())
                || LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.equals(copy.getTypeOf())) {
            copy.getLoanChargesPaid().addAll(copy.getLoanChargesPaid());
        }
        if (LoanTransactionType.REAGE.equals(copy.getTypeOf())) {
            copy.setLoanReAgeParameter(lt.getLoanReAgeParameter().getCopy(copy));
        }
        if (LoanTransactionType.REAMORTIZE.equals(copy.getTypeOf())) {
            copy.setLoanReAmortizationParameter(lt.getLoanReAmortizationParameter());
        }
        collect.add(copy);
        if (lt.getTypeOf().isDisbursement() && MathUtil.isGreaterThanZero(refundFinal.get())) {
            if (lt.getAmount().compareTo(refundFinal.get()) <= 0) {
                collect.add(new LoanTransaction(lt.getLoan(), lt.getLoan().getOffice(), REPAYMENT, lt.getDateOf(), lt.getAmount(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, null));
                refundFinal.set(refundFinal.get().subtract(lt.getAmount()));
            } else {
                collect.add(new LoanTransaction(lt.getLoan(), lt.getLoan().getOffice(), REPAYMENT, lt.getDateOf(), refundFinal.get(),
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, false, null, null));
                refundFinal.set(BigDecimal.ZERO);
            }
        }
    }

    private Money recalculateTotalInterest(PhasedSettlementScheduleProcessor processor, Loan loan,
            LocalDate relatedRefundTransactionDate, List<LoanTransaction> transactionsToReprocess) {

        final ScheduleGeneratorDTO scheduleGeneratorDTO = loanUtilService.buildScheduleGeneratorDTO(loan, null);
        loanScheduleService.regenerateRepaymentSchedule(loan, scheduleGeneratorDTO);

        Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> reprocessResult = processor
                .reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), relatedRefundTransactionDate, transactionsToReprocess,
                        loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());

        final List<LoanTransaction> newTransactions = reprocessResult.getLeft().getTransactionChanges().stream()
                .map(TransactionChangeData::getNewTransaction).toList().stream().filter(LoanTransaction::isNotReversed).toList();
        loan.getLoanTransactions().addAll(newTransactions);
        ProgressiveLoanInterestScheduleModel modelAfter = reprocessResult.getRight();

        return emiCalculator.getSumOfDueInterestsOnDate(modelAfter, relatedRefundTransactionDate);
    }

    @Override
    public boolean canHandle(Loan loan) {
        String s = loan.getTransactionProcessingStrategyCode();
        return PhasedSettlementScheduleProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY_NAME.equalsIgnoreCase(s)
                || PhasedSettlementScheduleProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY.equalsIgnoreCase(s);
    }

    private boolean isTransactionNeededForInterestRefundCalculations(LoanTransaction lt) {
        return lt.isNotReversed() && !lt.isAccrualRelated() && !lt.isInterestRefund();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Money totalInterestByTransactions(LoanRepaymentScheduleTransactionProcessor processor, final Long loanId,
            LocalDate relatedRefundTransactionDate, List<LoanTransaction> newTransactions, List<Long> oldTransactionIds,
            List<LoanTermVariations> activeLoanTermVariations) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        loan.setLoanTermVariations(activeLoanTermVariations);
        if (processor == null) {
            processor = loanTransactionProcessingService.getTransactionProcessor(loan.getTransactionProcessingStrategyCode());
        }
        if (!(processor instanceof PhasedSettlementScheduleProcessor)) {
            throw new IllegalArgumentException(
                    "Wrong processor implementation. ProgressiveLoanInterestRefundServiceImpl requires PhasedSettlementScheduleProcessor");
        }

        List<LoanTransaction> transactionsToReprocess = new ArrayList<>();
        List<LoanTransactionType> interestRefundTypes = loan.getSupportedInterestRefundTransactionTypes();

        List<LoanTransaction> transactions = Stream.concat(loan.getLoanTransactions().stream() //
                .filter(lt -> isTransactionNeededForInterestRefundCalculations(lt) //
                        && oldTransactionIds.contains(lt.getId())), //
                newTransactions.stream() //
                        .filter(this::isTransactionNeededForInterestRefundCalculations) //
                        .map(LoanTransaction::copyTransactionProperties)) //
                .toList();

        final AtomicReference<BigDecimal> refundFinal = new AtomicReference<>(
                transactions.stream().filter(lt -> interestRefundTypes.contains(lt.getTypeOf())) //
                        .map(LoanTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

        transactions.stream().filter(loanTransaction -> !interestRefundTypes.contains(loanTransaction.getTypeOf())) //
                .forEach(lt -> simulateRepaymentForDisbursements(lt, refundFinal, transactionsToReprocess)); //

        return recalculateTotalInterest((PhasedSettlementScheduleProcessor) processor, loan, relatedRefundTransactionDate,
                transactionsToReprocess);
    }
}
