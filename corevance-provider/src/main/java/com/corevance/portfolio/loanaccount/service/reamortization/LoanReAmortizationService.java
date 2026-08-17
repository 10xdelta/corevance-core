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
package com.corevance.portfolio.loanaccount.service.reamortization;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.codes.domain.CodeValue;
import com.corevance.infrastructure.codes.domain.CodeValueRepository;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.data.CommandProcessingResultBuilder;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.infrastructure.event.business.domain.loan.reamortization.LoanReAmortizeBusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.reamortization.LoanUndoReAmortizeBusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.transaction.reamortization.LoanReAmortizeTransactionBusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.transaction.reamortization.LoanUndoReAmortizeTransactionBusinessEvent;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.organisation.monetary.data.CurrencyData;
import com.corevance.organisation.monetary.domain.Money;
import com.corevance.portfolio.loanaccount.api.LoanApiConstants;
import com.corevance.portfolio.loanaccount.api.LoanReAmortizationApiConstants;
import com.corevance.portfolio.loanaccount.api.request.ReAmortizationPreviewRequest;
import com.corevance.portfolio.loanaccount.data.DisbursementData;
import com.corevance.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionType;
import com.corevance.portfolio.loanaccount.domain.reamortization.LoanReAmortizationInterestHandlingType;
import com.corevance.portfolio.loanaccount.domain.reamortization.LoanReAmortizationParameter;
import com.corevance.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import com.corevance.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import com.corevance.portfolio.loanaccount.serialization.LoanChargeValidator;
import com.corevance.portfolio.loanaccount.service.LoanAssembler;
import com.corevance.portfolio.loanaccount.service.LoanReadPlatformService;
import com.corevance.portfolio.loanaccount.service.LoanRepaymentScheduleService;
import com.corevance.portfolio.loanaccount.service.LoanScheduleService;
import com.corevance.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LoanReAmortizationService {

    private final LoanAssembler loanAssembler;
    private final LoanReAmortizationValidator reAmortizationValidator;
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanChargeValidator loanChargeValidator;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final CodeValueRepository codeValueRepository;
    private final LoanScheduleService loanScheduleService;
    private final LoanRepaymentScheduleService loanRepaymentScheduleService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;

    public CommandProcessingResult reAmortize(final Long loanId, final JsonCommand command) {
        final Loan loan = loanAssembler.assembleFrom(loanId);
        reAmortizationValidator.validateReAmortize(loan, command);

        final LoanTransaction reAmortizeTransaction = createReAmortizeTransaction(loan, command);
        reAmortizeTransaction.setLoanReAmortizationParameter(createReAmortizationParameter(reAmortizeTransaction, command));
        processReAmortizationTransaction(loan, reAmortizeTransaction, true);
        loanTransactionRepository.saveAndFlush(reAmortizeTransaction);

        final Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanReAmortizationApiConstants.localeParameterName, command.locale());
        changes.put(LoanReAmortizationApiConstants.dateFormatParameterName, command.dateFormat());

        // delinquency recalculation will be triggered by the event in a decoupled way via a listener
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReAmortizeBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanReAmortizeTransactionBusinessEvent(reAmortizeTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(reAmortizeTransaction.getId()) //
                .withEntityExternalId(reAmortizeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes) //
                .build();
    }

    public CommandProcessingResult undoReAmortize(Long loanId, JsonCommand command) {
        Loan loan = loanAssembler.assembleFrom(loanId);
        final LoanTransaction reAmortizeTransaction = reAmortizationValidator.findAndValidateReAmortizeTransactionForUndo(loan);

        Map<String, Object> changes = new LinkedHashMap<>();
        changes.put(LoanReAmortizationApiConstants.localeParameterName, command.locale());
        changes.put(LoanReAmortizationApiConstants.dateFormatParameterName, command.dateFormat());

        if (loan.isProgressiveSchedule()) {
            loanScheduleService.regenerateRepaymentSchedule(loan);
        }
        reverseReAmortizeTransaction(reAmortizeTransaction, command);
        loanTransactionRepository.saveAndFlush(reAmortizeTransaction);

        // delinquency recalculation will be triggered by the event in a decoupled way via a listener
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoReAmortizeBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanUndoReAmortizeTransactionBusinessEvent(reAmortizeTransaction));
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(reAmortizeTransaction.getId()) //
                .withEntityExternalId(reAmortizeTransaction.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(command.getLoanId()) //
                .with(changes) //
                .build();
    }

    @Transactional(readOnly = true)
    public LoanScheduleData previewReAmortization(final Long loanId, final String loanExternalId,
            final ReAmortizationPreviewRequest reAmortizationPreviewRequest) {
        final Loan loan = loanId != null ? loanAssembler.assembleFrom(loanId)
                : loanAssembler.assembleFrom(ExternalIdFactory.produce(loanExternalId), false);
        return previewReAmortization(loan, reAmortizationPreviewRequest);
    }

    private LoanScheduleData previewReAmortization(final Loan loan, final ReAmortizationPreviewRequest reAmortizationPreviewRequest) {
        reAmortizationValidator.validateReAmortize(loan, reAmortizationPreviewRequest);

        final LoanTransaction reAmortizeTransaction = createReAmortizeTransactionFromPreviewRequest(loan, reAmortizationPreviewRequest);
        processReAmortizationTransaction(loan, reAmortizeTransaction, false);
        loan.updateLoanScheduleDependentDerivedFields();

        final CurrencyData currencyData = new CurrencyData(loan.getCurrencyCode(), null, loan.getCurrency().getDigitsAfterDecimal(),
                loan.getCurrency().getInMultiplesOf(), null, null);
        final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedLoanData = new RepaymentScheduleRelatedLoanData(
                loan.getDisbursementDate(), loan.getDisbursementDate(), currencyData, loan.getPrincipal().getAmount(),
                loan.getInArrearsTolerance().getAmount(), ZERO);
        final Collection<DisbursementData> disbursementData = loanReadPlatformService.retrieveLoanDisbursementDetails(loan.getId());
        final Collection<LoanTransactionRepaymentPeriodData> capitalizedIncomeData = loanCapitalizedIncomeBalanceRepository
                .findRepaymentPeriodDataByLoanId(loan.getId());
        final List<LoanRepaymentScheduleInstallment> sortedInstallments = loan.getRepaymentScheduleInstallments().stream()
                .sorted(Comparator.comparingInt(LoanRepaymentScheduleInstallment::getInstallmentNumber)).collect(Collectors.toList());

        return loanRepaymentScheduleService.extractLoanScheduleData(sortedInstallments, repaymentScheduleRelatedLoanData, disbursementData,
                capitalizedIncomeData, loan.isInterestRecalculationEnabled(), loan.getLoanProductRelatedDetail().getLoanScheduleType());
    }

    private void reverseReAmortizeTransaction(LoanTransaction reAmortizeTransaction, JsonCommand command) {
        ExternalId reversalExternalId = externalIdFactory.createFromCommand(command,
                LoanReAmortizationApiConstants.externalIdParameterName);
        final Loan loan = reAmortizeTransaction.getLoan();
        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(loan, reAmortizeTransaction, "reversed");
        reAmortizeTransaction.reverse(reversalExternalId);
        reAmortizeTransaction.manuallyAdjustedOrReversed();
        reprocessLoanTransactionsService.reprocessTransactions(loan);
    }

    private LoanTransaction createReAmortizeTransaction(Loan loan, JsonCommand command) {
        ExternalId txExternalId = externalIdFactory.createFromCommand(command, LoanReAmortizationApiConstants.externalIdParameterName);

        // reamortize transaction date is always the current business date
        LocalDate transactionDate = DateUtils.getBusinessLocalDate();

        // in case of a reamortize transaction, only the outstanding principal amount until the business date is
        // considered
        Money txPrincipal = loan.getTotalPrincipalOutstandingUntil(transactionDate);
        BigDecimal txPrincipalAmount = txPrincipal.getAmount();

        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.REAMORTIZE, transactionDate, txPrincipalAmount,
                txPrincipalAmount, ZERO, ZERO, ZERO, null, false, null, txExternalId);
    }

    private LoanTransaction createReAmortizeTransactionFromPreviewRequest(final Loan loan,
            final ReAmortizationPreviewRequest reAmortizationPreviewRequest) {
        // re-amortization transaction date is always the current business date
        final LocalDate transactionDate = DateUtils.getBusinessLocalDate();
        final Money txPrincipal = loan.getTotalPrincipalOutstandingUntil(transactionDate);
        final BigDecimal txPrincipalAmount = txPrincipal.getAmount();

        final LoanTransaction reAmortizationTransaction = new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.REAMORTIZE,
                transactionDate, txPrincipalAmount, txPrincipalAmount, ZERO, ZERO, ZERO, null, false, null, null);

        final LoanReAmortizationParameter reAmortizationParameter = createReAmortizationParameterFromPreviewRequest(
                reAmortizationTransaction, reAmortizationPreviewRequest);
        reAmortizationTransaction.setLoanReAmortizationParameter(reAmortizationParameter);

        return reAmortizationTransaction;
    }

    private LoanReAmortizationParameter createReAmortizationParameter(LoanTransaction reAmortizationTransaction, JsonCommand command) {
        LoanReAmortizationInterestHandlingType reAmortizationInterestHandlingType = command.enumValueOfParameterNamed(
                LoanReAmortizationApiConstants.reAmortizationInterestHandlingParamName, LoanReAmortizationInterestHandlingType.class);
        if (reAmortizationInterestHandlingType == null) {
            reAmortizationInterestHandlingType = LoanReAmortizationInterestHandlingType.DEFAULT;
        }

        CodeValue reasonCodeValue = null;
        if (command.parameterExists(LoanReAmortizationApiConstants.reasonCodeValueIdParamName)) {
            reasonCodeValue = codeValueRepository.findByCodeNameAndId(LoanApiConstants.REAMORTIZATION_REASONS,
                    command.longValueOfParameterNamed(LoanReAmortizationApiConstants.reasonCodeValueIdParamName));
        }

        return new LoanReAmortizationParameter(reAmortizationTransaction, reAmortizationInterestHandlingType, reasonCodeValue);
    }

    private LoanReAmortizationParameter createReAmortizationParameterFromPreviewRequest(final LoanTransaction reAmortizationTransaction,
            final ReAmortizationPreviewRequest reAmortizationPreviewRequest) {
        final LoanReAmortizationInterestHandlingType reAmortizationInterestHandlingType = LoanReAmortizationInterestHandlingType
                .valueOf(reAmortizationPreviewRequest.getReAmortizationInterestHandling());
        return new LoanReAmortizationParameter(reAmortizationTransaction, reAmortizationInterestHandlingType, null);
    }

    private void processReAmortizationTransaction(final Loan loan, final LoanTransaction reAmortizationTransaction,
            final boolean withPostTransactionChecks) {
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanScheduleService.regenerateRepaymentSchedule(loan);
            if (withPostTransactionChecks) {
                reprocessLoanTransactionsService.reprocessTransactions(loan, List.of(reAmortizationTransaction));
            } else {
                reprocessLoanTransactionsService.reprocessTransactionsWithoutChecks(loan, List.of(reAmortizationTransaction));
            }
        } else {
            reprocessLoanTransactionsService.processLatestTransaction(reAmortizationTransaction, loan);
        }
    }
}
