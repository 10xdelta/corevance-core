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
package com.corevance.portfolio.loanaccount.service.adjustment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import com.corevance.infrastructure.core.data.ApiParameterError;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.data.CommandProcessingResultBuilder;
import com.corevance.infrastructure.core.data.DataValidatorBuilder;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.infrastructure.core.exception.PlatformApiDataValidationException;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.infrastructure.core.service.MathUtil;
import com.corevance.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.LoanBalanceChangedBusinessEvent;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.organisation.monetary.domain.MonetaryCurrency;
import com.corevance.organisation.monetary.domain.Money;
import com.corevance.portfolio.loanaccount.api.LoanApiConstants;
import com.corevance.portfolio.loanaccount.data.HolidayDetailDTO;
import com.corevance.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanAccountDomainService;
import com.corevance.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import com.corevance.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import com.corevance.portfolio.loanaccount.domain.LoanCharge;
import com.corevance.portfolio.loanaccount.domain.LoanEvent;
import com.corevance.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionType;
import com.corevance.portfolio.loanaccount.exception.InvalidLoanTransactionTypeException;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import com.corevance.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import com.corevance.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import com.corevance.portfolio.loanaccount.serialization.LoanChargeValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanTransactionValidator;
import com.corevance.portfolio.loanaccount.service.LoanAccrualsProcessingService;
import com.corevance.portfolio.loanaccount.service.LoanBalanceService;
import com.corevance.portfolio.loanaccount.service.LoanDownPaymentHandlerService;
import com.corevance.portfolio.loanaccount.service.LoanJournalEntryPoster;
import com.corevance.portfolio.loanaccount.service.LoanScheduleService;
import com.corevance.portfolio.loanaccount.service.LoanUtilService;
import com.corevance.portfolio.loanaccount.service.ReprocessLoanTransactionsService;
import com.corevance.portfolio.note.domain.Note;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.paymentdetail.domain.PaymentDetail;
import com.corevance.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LoanAdjustmentServiceImpl implements LoanAdjustmentService {

    private final LoanTransactionValidator loanTransactionValidator;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanAccountDomainService loanAccountDomainService;
    private final NoteRepository noteRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanUtilService loanUtilService;
    private final LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;
    private final LoanLifecycleStateMachine loanLifecycleStateMachine;
    private final LoanDownPaymentHandlerService loanDownPaymentHandlerService;
    private final LoanAccrualsProcessingService loanAccrualsProcessingService;
    private final LoanChargeValidator loanChargeValidator;
    private final LoanJournalEntryPoster journalEntryPoster;
    private final LoanBalanceService loanBalanceService;
    private final ReprocessLoanTransactionsService reprocessLoanTransactionsService;
    private final LoanCapitalizedIncomeBalanceRepository loanCapitalizedIncomeBalanceRepository;
    private final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository;
    private final LoanScheduleService loanScheduleService;

    @Override
    public CommandProcessingResult adjustLoanTransaction(Loan loan, LoanTransaction transactionToAdjust, LoanAdjustmentParameter parameter,
            Long commandId, Map<String, Object> changes) {
        LocalDate transactionDate = parameter.getTransactionDate();
        BigDecimal transactionAmount = parameter.getTransactionAmount();
        PaymentDetail paymentDetail = parameter.getPaymentDetail();
        ExternalId txnExternalId = parameter.getTxnExternalId();
        ExternalId reversalTxnExternalId = parameter.getReversalTxnExternalId();
        String noteText = parameter.getNoteText();

        final Money transactionAmountAsMoney = Money.of(loan.getCurrency(), transactionAmount);
        LoanTransaction newTransactionDetail = LoanTransaction.repaymentType(transactionToAdjust.getTypeOf(), loan.getOffice(),
                transactionAmountAsMoney, paymentDetail, transactionDate, txnExternalId, transactionToAdjust.getChargeRefundChargeType());
        if (transactionToAdjust.isInterestWaiver()) {
            Money unrecognizedIncome = transactionAmountAsMoney.zero();
            Money interestComponent = transactionAmountAsMoney;
            if (loan.isPeriodicAccrualAccountingEnabledOnLoanProduct()) {
                Money receivableInterest = loanBalanceService.getReceivableInterest(loan, transactionDate);
                if (transactionAmountAsMoney.isGreaterThan(receivableInterest)) {
                    interestComponent = receivableInterest;
                    unrecognizedIncome = transactionAmountAsMoney.minus(receivableInterest);
                }
            }
            newTransactionDetail = LoanTransaction.waiver(loan.getOffice(), loan, transactionAmountAsMoney, transactionDate,
                    interestComponent, unrecognizedIncome, txnExternalId);
        }
        if (transactionToAdjust.isChargesWaiver()) {
            transactionToAdjust.getLoanChargesPaid().forEach(loanChargePaidBy -> {
                LoanCharge loanCharge = loanChargePaidBy.getLoanCharge();
                MonetaryCurrency currency = loanCharge.getLoan().getCurrency();

                Integer installmentNumber = loanChargePaidBy.getInstallmentNumber();

                loanCharge.undoWaive(currency, installmentNumber);
            });
        }

        if (transactionToAdjust.isCapitalizedIncome()) {
            if (newTransactionDetail.isNotZero()) {
                throw new InvalidLoanTransactionTypeException("transaction", "capitalizedIncome.cannot.be.adjusted",
                        "Capitalized income transaction cannot be adjusted");
            }

            LoanCapitalizedIncomeBalance capitalizedIncomeBalance = loanCapitalizedIncomeBalanceRepository
                    .findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(loan.getId(), transactionToAdjust.getId());
            if (MathUtil.isGreaterThanZero(capitalizedIncomeBalance.getAmountAdjustment())) {
                throw new InvalidLoanTransactionTypeException("transaction", "capitalizedIncome.cannot.be.reversed.when.adjusted",
                        "Capitalized income transaction cannot be reversed when non-reversed adjustment exists for it.");
            }
            capitalizedIncomeBalance.setDeleted(true);
            loanCapitalizedIncomeBalanceRepository.saveAndFlush(capitalizedIncomeBalance);
        }
        if (transactionToAdjust.isCapitalizedIncomeAdjustment()) {
            if (newTransactionDetail.isNotZero()) {
                throw new InvalidLoanTransactionTypeException("transaction", "capitalizedIncomeAdjustment.cannot.be.adjusted",
                        "Capitalized income adjustment transaction cannot be adjusted");
            }

            LoanCapitalizedIncomeBalance capitalizedIncomeBalance = loanCapitalizedIncomeBalanceRepository
                    .findBalanceForAdjustment(transactionToAdjust.getId());

            capitalizedIncomeBalance
                    .setAmountAdjustment(capitalizedIncomeBalance.getAmountAdjustment().subtract(transactionToAdjust.getAmount()));
            capitalizedIncomeBalance
                    .setUnrecognizedAmount(capitalizedIncomeBalance.getUnrecognizedAmount().add(transactionToAdjust.getAmount()));
        }
        if (transactionToAdjust.isBuyDownFee()) {
            if (newTransactionDetail.isNotZero()) {
                throw new InvalidLoanTransactionTypeException("transaction", "buy.down.fee.cannot.be.adjusted",
                        "Buy down fee transaction cannot be adjusted");
            }

            LoanBuyDownFeeBalance buyDownFeeBalance = loanBuyDownFeeBalanceRepository
                    .findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(loan.getId(), transactionToAdjust.getId());

            if (MathUtil.isGreaterThanZero(buyDownFeeBalance.getAmountAdjustment())) {
                throw new InvalidLoanTransactionTypeException("transaction", "buy.down.fee.cannot.be.reversed.when.adjusted",
                        "Buy down fee transaction cannot be reversed when non-reversed adjustment exists for it.");
            }
            buyDownFeeBalance.setDeleted(true);
            loanBuyDownFeeBalanceRepository.saveAndFlush(buyDownFeeBalance);
        }
        if (transactionToAdjust.isBuyDownFeeAdjustment()) {
            if (newTransactionDetail.isNotZero()) {
                throw new InvalidLoanTransactionTypeException("transaction", "buy.down.fee.adjustment.cannot.be.adjusted",
                        "Buy down fee adjustment transaction cannot be adjusted");
            }
            LoanBuyDownFeeBalance buyDownFeeBalance = loanBuyDownFeeBalanceRepository.findBalanceForAdjustment(transactionToAdjust.getId());

            buyDownFeeBalance.setAmountAdjustment(buyDownFeeBalance.getAmountAdjustment().subtract(transactionToAdjust.getAmount()));
            buyDownFeeBalance.setUnrecognizedAmount(buyDownFeeBalance.getUnrecognizedAmount().add(transactionToAdjust.getAmount()));
        }

        LocalDate recalculateFrom = null;

        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            recalculateFrom = DateUtils.isAfter(transactionToAdjust.getTransactionDate(), transactionDate) ? transactionDate
                    : transactionToAdjust.getTransactionDate();
        }

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan, recalculateFrom);

        HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
        if (loan.getLoanRepaymentScheduleDetail().getLoanScheduleType().equals(LoanScheduleType.CUMULATIVE)) {
            // validate cumulative
            loanTransactionValidator.validateActivityNotBeforeLastTransactionDate(loan, transactionToAdjust.getTransactionDate(),
                    LoanEvent.LOAN_REPAYMENT_OR_WAIVER);
        }
        // common validations
        loanTransactionValidator.validateRepaymentDateIsOnHoliday(newTransactionDetail.getTransactionDate(),
                holidayDetailDTO.isAllowTransactionsOnHoliday(), holidayDetailDTO.getHolidays());
        loanTransactionValidator.validateRepaymentDateIsOnNonWorkingDay(newTransactionDetail.getTransactionDate(),
                holidayDetailDTO.getWorkingDays(), holidayDetailDTO.isAllowTransactionsOnNonWorkingDay());

        adjustExistingTransaction(loan, newTransactionDetail, transactionToAdjust, scheduleGeneratorDTO, reversalTxnExternalId);

        loanAccrualsProcessingService.reprocessExistingAccruals(loan, true);
        if (loan.isInterestBearingAndInterestRecalculationEnabled()) {
            loanAccrualsProcessingService.processIncomePostingAndAccruals(loan, true);
        }

        boolean thereIsNewTransaction = newTransactionDetail.isGreaterThanZero();
        if (thereIsNewTransaction) {
            if (paymentDetail != null) {
                this.paymentDetailWritePlatformService.persistPaymentDetail(paymentDetail);
            }
            this.loanTransactionRepository.saveAndFlush(newTransactionDetail);
            journalEntryPoster.postJournalEntriesForLoanTransaction(newTransactionDetail, false, false);
        }

        loan = saveAndFlushLoanWithDataIntegrityViolationChecks(loan);

        if (StringUtils.isNotBlank(noteText)) {
            changes.put("note", noteText);
            Note note;
            /*
             * If a new transaction is not created, associate note with the transaction to be adjusted
             */
            if (thereIsNewTransaction) {
                note = Note.loanTransactionNote(loan, newTransactionDetail, noteText);
            } else {
                note = Note.loanTransactionNote(loan, transactionToAdjust, noteText);
            }
            this.noteRepository.save(note);
        }

        loanLifecycleStateMachine.determineAndTransition(loan, loan.getLastUserTransactionDate());

        loanAccrualsProcessingService.processAccrualsOnInterestRecalculation(loan, loan.isInterestBearingAndInterestRecalculationEnabled(),
                true);

        this.loanAccountDomainService.setLoanDelinquencyTag(loan, DateUtils.getBusinessLocalDate());

        LoanAdjustTransactionBusinessEvent.Data eventData = new LoanAdjustTransactionBusinessEvent.Data(transactionToAdjust);
        if (newTransactionDetail.isRepaymentLikeType() && thereIsNewTransaction) {
            eventData.setNewTransactionDetail(newTransactionDetail);
        }
        Long entityId = transactionToAdjust.getId();
        ExternalId entityExternalId = transactionToAdjust.getExternalId();

        if (thereIsNewTransaction) {
            entityId = newTransactionDetail.getId();
            entityExternalId = newTransactionDetail.getExternalId();
        }

        journalEntryPoster.postJournalEntriesForLoanTransaction(transactionToAdjust, false, false);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBalanceChangedBusinessEvent(loan));
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(eventData));

        return new CommandProcessingResultBuilder() //
                .withCommandId(commandId) //
                .withEntityId(entityId) //
                .withEntityExternalId(entityExternalId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withGroupId(loan.getGroupId()) //
                .withLoanId(loan.getId()) //
                .with(changes) //
                .build();
    }

    public void adjustExistingTransaction(final Loan loan, final LoanTransaction newTransactionDetail,
            final LoanTransaction transactionForAdjustment, final ScheduleGeneratorDTO scheduleGeneratorDTO,
            final ExternalId reversalExternalId) {
        loanTransactionValidator.validateActivityNotBeforeClientOrGroupTransferDate(loan, LoanEvent.LOAN_REPAYMENT_OR_WAIVER,
                transactionForAdjustment.getTransactionDate());

        if (!transactionForAdjustment.isAccrualRelated() && transactionForAdjustment.isNotRepaymentLikeType()
                && transactionForAdjustment.isNotWaiver() && transactionForAdjustment.isNotCreditBalanceRefund()
                && !transactionForAdjustment.isDeferredIncome() && !transactionForAdjustment.isCapitalizedIncomeAdjustment()
                && !transactionForAdjustment.isBuyDownFeeAdjustment()) {
            final String errorMessage = "Only (non-reversed) transactions of type repayment, waiver, accrual, credit balance refund, capitalized income, capitalized income adjustment, buy down fee or buy down fee adjustment can be adjusted.";
            throw new InvalidLoanTransactionTypeException("transaction",
                    "adjustment.is.only.allowed.to.repayment.or.waiver.or.creditbalancerefund.or.capitalizedIncome.or.capitalizedIncomeAdjustment.or.buyDownFee.or.buyDownFeeAdjustment.transactions",
                    errorMessage);
        }

        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(transactionForAdjustment.getLoan(),
                transactionForAdjustment, "reversed");
        transactionForAdjustment.reverse(reversalExternalId);
        transactionForAdjustment.manuallyAdjustedOrReversed();

        if (transactionForAdjustment.getTypeOf().equals(LoanTransactionType.MERCHANT_ISSUED_REFUND)
                || transactionForAdjustment.getTypeOf().equals(LoanTransactionType.PAYOUT_REFUND)) {
            loan.getLoanTransactions().stream() //
                    .filter(LoanTransaction::isNotReversed)
                    .filter(loanTransaction -> loanTransaction.getLoanTransactionRelations().stream()
                            .anyMatch(relation -> relation.getRelationType().equals(LoanTransactionRelationTypeEnum.RELATED)
                                    && relation.getToTransaction().getId().equals(transactionForAdjustment.getId())))
                    .forEach(loanTransaction -> {
                        loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(loanTransaction.getLoan(),
                                loanTransaction, "reversed");
                        loanTransaction.reverse();
                        loanTransaction.manuallyAdjustedOrReversed();
                        journalEntryPoster.postJournalEntriesForLoanTransaction(loanTransaction, false, false);
                        LoanAdjustTransactionBusinessEvent.Data eventData = new LoanAdjustTransactionBusinessEvent.Data(loanTransaction);
                        businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(eventData));
                    });
        }

        if (loan.isClosedWrittenOff()) {
            // find write off transaction and reverse it
            final LoanTransaction writeOffTransaction = loan.findWriteOffTransaction();
            loanChargeValidator.validateRepaymentTypeTransactionNotBeforeAChargeRefund(writeOffTransaction.getLoan(), writeOffTransaction,
                    "reversed");
            writeOffTransaction.reverse();
        }

        if (newTransactionDetail.isRepaymentLikeType() || newTransactionDetail.isWaiver()) {
            loanDownPaymentHandlerService.handleRepaymentOrRecoveryOrWaiverTransaction(loan, newTransactionDetail, transactionForAdjustment,
                    scheduleGeneratorDTO);
        }

        if (transactionForAdjustment.getTypeOf().equals(LoanTransactionType.CAPITALIZED_INCOME)) {
            loanScheduleService.regenerateScheduleWithReprocessingTransactions(loan);
        }
    }

    private Loan saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        /*
         * Due to the "saveAndFlushLoanWithDataIntegrityViolationChecks" method the loan is saved and flushed in the
         * middle of the transaction. EclipseLink is in some situations are saving inconsistently the newly created
         * associations, like the newly created repayment schedule installments. The save and flush cannot be removed
         * safely till any native queries are used as part of this transaction either. See:
         * this.loanAccountDomainService.recalculateAccruals(loan);
         */
        try {
            loanRepaymentScheduleInstallmentRepository.saveAll(loan.getRepaymentScheduleInstallments());
            return this.loanRepositoryWrapper.saveAndFlush(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.transaction");
            if (realCause.getMessage().toLowerCase(java.util.Locale.ROOT).contains("external_id_unique")) {
                baseDataValidator.reset().parameter(LoanApiConstants.externalIdParameterName).failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
            throw e;
        }
    }
}
