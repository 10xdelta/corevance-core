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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.corevance.infrastructure.codes.domain.CodeValueRepository;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.data.CommandProcessingResultBuilder;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.infrastructure.core.service.MathUtil;
import com.corevance.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import com.corevance.infrastructure.event.business.domain.loan.transaction.LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.transaction.LoanBuyDownFeeTransactionCreatedBusinessEvent;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.organisation.monetary.domain.Money;
import com.corevance.portfolio.client.domain.Client;
import com.corevance.portfolio.client.exception.ClientNotActiveException;
import com.corevance.portfolio.group.domain.Group;
import com.corevance.portfolio.group.exception.GroupNotActiveException;
import com.corevance.portfolio.loanaccount.api.LoanTransactionApiConstants;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRelation;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.repository.LoanBuyDownFeeBalanceRepository;
import com.corevance.portfolio.note.data.NoteCreateRequest;
import com.corevance.portfolio.note.domain.NoteType;
import com.corevance.portfolio.paymentdetail.domain.PaymentDetail;
import com.corevance.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class BuyDownFeeWritePlatformServiceImpl implements BuyDownFeePlatformService {

    private final ProgressiveLoanTransactionValidator loanTransactionValidator;
    private final LoanAssembler loanAssembler;
    private final LoanTransactionRepository loanTransactionRepository;
    private final PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    private final LoanJournalEntryPoster loanJournalEntryPoster;
    private final ExternalIdFactory externalIdFactory;
    private final LoanBuyDownFeeBalanceRepository loanBuyDownFeeBalanceRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final CodeValueRepository codeValueRepository;
    private final TransactionBoundApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public CommandProcessingResult makeLoanBuyDownFee(final Long loanId, final JsonCommand command) {

        this.loanTransactionValidator.validateBuyDownFee(command, loanId);

        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final Map<String, Object> changes = new LinkedHashMap<>();

        // Create payment details
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        // Extract transaction details
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, "externalId");

        // Create buy down fee transaction
        final Money buyDownFeeAmount = Money.of(loan.getCurrency(), transactionAmount); // FLAT calculation
        final LoanTransaction buyDownFeeTransaction = LoanTransaction.buyDownFee(loan, buyDownFeeAmount, paymentDetail, transactionDate,
                txnExternalId);

        // Add to loan (NO schedule recalculation as per requirements)
        loan.addLoanTransaction(buyDownFeeTransaction);

        // Add Loan Transaction classification
        addClassificationCodeToTransaction(command, LoanTransactionApiConstants.BUY_DOWN_FEE_CLASSIFICATION_CODE, buyDownFeeTransaction);

        // Save transaction
        loanTransactionRepository.saveAndFlush(buyDownFeeTransaction);

        // Create Buy Down Fee balance
        createBuyDownFeeBalance(buyDownFeeTransaction);

        // Add note if provided
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            eventPublisher.publishEvent(NoteCreateRequest.builder().type(NoteType.LOAN_TRANSACTION)
                    .resourceId(buyDownFeeTransaction.getId()).note(noteText).build());
        }

        loanJournalEntryPoster.postJournalEntriesForLoanTransaction(buyDownFeeTransaction, false, false);

        // Notify business events
        businessEventNotifierService.notifyPostBusinessEvent(new LoanBuyDownFeeTransactionCreatedBusinessEvent(buyDownFeeTransaction));

        return new CommandProcessingResultBuilder() //
                .withClientId(loan.getClientId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withLoanId(loan.getId()) //
                .withEntityId(buyDownFeeTransaction.getId()) //
                .withEntityExternalId(buyDownFeeTransaction.getExternalId()) //
                .build();
    }

    @Override
    @Transactional
    public CommandProcessingResult buyDownFeeAdjustment(final Long loanId, final Long buyDownFeeTransactionId, final JsonCommand command) {
        this.loanTransactionValidator.validateBuyDownFeeAdjustment(command, loanId, buyDownFeeTransactionId);
        final Loan loan = this.loanAssembler.assembleFrom(loanId);
        checkClientOrGroupActive(loan);

        final Map<String, Object> changes = new LinkedHashMap<>();

        // Create payment details
        final PaymentDetail paymentDetail = this.paymentDetailWritePlatformService.createAndPersistPaymentDetail(command, changes);

        // Extract transaction details
        final LocalDate transactionDate = command.localDateValueOfParameterNamed("transactionDate");
        final BigDecimal transactionAmount = command.bigDecimalValueOfParameterNamed("transactionAmount");
        final ExternalId txnExternalId = externalIdFactory.createFromCommand(command, "externalId");

        // Find and validate original buy down fee transaction
        Optional<LoanTransaction> originalBuyDownFee = loanTransactionRepository.findById(buyDownFeeTransactionId);
        if (originalBuyDownFee.isEmpty() || !originalBuyDownFee.get().isBuyDownFee()) {
            throw new IllegalArgumentException("Original transaction must be a valid Buy Down Fee transaction");
        }

        // Create buy down fee adjustment transaction
        LoanTransaction buyDownFeeAdjustment = LoanTransaction.buyDownFeeAdjustment(loan, Money.of(loan.getCurrency(), transactionAmount),
                paymentDetail, transactionDate, txnExternalId);

        // Link to original transaction
        buyDownFeeAdjustment.getLoanTransactionRelations().add(LoanTransactionRelation.linkToTransaction(buyDownFeeAdjustment,
                originalBuyDownFee.get(), LoanTransactionRelationTypeEnum.ADJUSTMENT));

        // Inherit from the target transaction the classification
        buyDownFeeAdjustment.setClassification(originalBuyDownFee.get().getClassification());
        // Add transaction to loan
        loan.addLoanTransaction(buyDownFeeAdjustment);

        // Save transaction
        LoanTransaction savedBuyDownFeeAdjustment = loanTransactionRepository.saveAndFlush(buyDownFeeAdjustment);

        // Update buy down fee balance
        LoanBuyDownFeeBalance buydownFeeBalance = loanBuyDownFeeBalanceRepository
                .findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(loanId, buyDownFeeTransactionId);
        if (buydownFeeBalance != null) {
            buydownFeeBalance.setAmountAdjustment(MathUtil.nullToZero(buydownFeeBalance.getAmountAdjustment()).add(transactionAmount));
            buydownFeeBalance
                    .setUnrecognizedAmount(MathUtil.negativeToZero(buydownFeeBalance.getUnrecognizedAmount().subtract(transactionAmount)));
            loanBuyDownFeeBalanceRepository.save(buydownFeeBalance);
        }

        // Create a note if provided
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            eventPublisher.publishEvent(NoteCreateRequest.builder().type(NoteType.LOAN_TRANSACTION)
                    .resourceId(savedBuyDownFeeAdjustment.getId()).note(noteText).build());
        }

        loanJournalEntryPoster.postJournalEntriesForLoanTransaction(savedBuyDownFeeAdjustment, false, false);

        // Notify business events
        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanBuyDownFeeAdjustmentTransactionCreatedBusinessEvent(savedBuyDownFeeAdjustment));

        return new CommandProcessingResultBuilder() //
                .withEntityId(savedBuyDownFeeAdjustment.getId()) //
                .withEntityExternalId(savedBuyDownFeeAdjustment.getExternalId()) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .withLoanId(loan.getId()) //
                .build();
    }

    private void checkClientOrGroupActive(final Loan loan) {
        final Client client = loan.client();
        if (client != null && client.isNotActive()) {
            throw new ClientNotActiveException(client.getId());
        }
        final Group group = loan.group();
        if (group != null && group.isNotActive()) {
            throw new GroupNotActiveException(group.getId());
        }
    }

    private void createBuyDownFeeBalance(final LoanTransaction buyDownFeeTransaction) {
        LoanBuyDownFeeBalance buyDownFeeBalance = new LoanBuyDownFeeBalance();
        buyDownFeeBalance.setLoan(buyDownFeeTransaction.getLoan());
        buyDownFeeBalance.setLoanTransaction(buyDownFeeTransaction);
        buyDownFeeBalance.setDate(buyDownFeeTransaction.getTransactionDate());
        buyDownFeeBalance.setAmount(buyDownFeeTransaction.getAmount());
        buyDownFeeBalance.setUnrecognizedAmount(buyDownFeeTransaction.getAmount());
        loanBuyDownFeeBalanceRepository.saveAndFlush(buyDownFeeBalance);
    }

    private void addClassificationCodeToTransaction(final JsonCommand command, final String codeName, LoanTransaction loanTransaction) {
        final Long transactionClassificationId = command
                .longValueOfParameterNamed(LoanTransactionApiConstants.TRANSACTION_CLASSIFICATIONID_PARAMNAME);
        if (transactionClassificationId != null) {
            loanTransaction.setClassification(codeValueRepository.findByCodeNameAndId(codeName, transactionClassificationId));
        }
    }
}
