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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.corevance.accounting.journalentry.service.JournalEntryWritePlatformService;
import com.corevance.cob.service.AccountLockService;
import com.corevance.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.infrastructure.core.exception.ErrorHandler;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import com.corevance.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.organisation.holiday.domain.HolidayRepositoryWrapper;
import com.corevance.organisation.teller.data.CashierTransactionDataValidator;
import com.corevance.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import com.corevance.portfolio.account.domain.AccountAssociationsRepository;
import com.corevance.portfolio.account.domain.AccountTransferDetailRepository;
import com.corevance.portfolio.account.domain.AccountTransferRepository;
import com.corevance.portfolio.account.service.AccountAssociationsReadPlatformService;
import com.corevance.portfolio.account.service.AccountTransfersReadPlatformService;
import com.corevance.portfolio.account.service.AccountTransfersWritePlatformService;
import com.corevance.portfolio.calendar.domain.CalendarInstanceRepository;
import com.corevance.portfolio.calendar.domain.CalendarRepository;
import com.corevance.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import com.corevance.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanAccountDomainService;
import com.corevance.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import com.corevance.portfolio.loanaccount.domain.LoanRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRelation;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRelationRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionType;
import com.corevance.portfolio.loanaccount.guarantor.service.GuarantorDomainService;
import com.corevance.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryWritePlatformService;
import com.corevance.portfolio.loanaccount.serialization.LoanApplicationValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanChargeValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanTransactionValidator;
import com.corevance.portfolio.loanaccount.serialization.LoanUpdateCommandFromApiJsonDeserializer;
import com.corevance.portfolio.loanaccount.service.adjustment.LoanAdjustmentServiceImpl;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.paymentdetail.service.PaymentDetailWritePlatformService;
import com.corevance.portfolio.repaymentwithpostdatedchecks.domain.PostDatedChecksRepository;
import com.corevance.portfolio.repaymentwithpostdatedchecks.service.RepaymentWithPostDatedChecksAssembler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanAdjustmentServiceImplTest {

    @InjectMocks
    private LoanAdjustmentServiceImpl underTest;

    @Mock
    private LoanRepaymentScheduleTransactionProcessorFactory transactionProcessorFactory;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private LoanTransactionValidator loanTransactionValidator;
    @Mock
    private LoanUpdateCommandFromApiJsonDeserializer loanUpdateCommandFromApiJsonDeserializer;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private LoanAccountDomainService loanAccountDomainService;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private LoanTransactionRelationRepository loanTransactionRelationRepository;
    @Mock
    private LoanAssembler loanAssembler;
    @Mock
    private JournalEntryWritePlatformService journalEntryWritePlatformService;
    @Mock
    private CalendarInstanceRepository calendarInstanceRepository;
    @Mock
    private PaymentDetailWritePlatformService paymentDetailWritePlatformService;
    @Mock
    private HolidayRepositoryWrapper holidayRepository;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private WorkingDaysRepositoryWrapper workingDaysRepository;
    @Mock
    private AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    @Mock
    private AccountTransfersReadPlatformService accountTransfersReadPlatformService;
    @Mock
    private AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private FromJsonHelper fromApiJsonHelper;
    @Mock
    private CalendarRepository calendarRepository;
    @Mock
    private LoanScheduleHistoryWritePlatformService loanScheduleHistoryWritePlatformService;
    @Mock
    private LoanApplicationValidator loanApplicationCommandFromApiJsonHelper;
    @Mock
    private AccountAssociationsRepository accountAssociationRepository;
    @Mock
    private AccountTransferDetailRepository accountTransferDetailRepository;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private GuarantorDomainService guarantorDomainService;
    @Mock
    private LoanUtilService loanUtilService;
    @Mock
    private EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    @Mock
    private LoanRepaymentScheduleTransactionProcessorFactory transactionProcessingStrategy;
    @Mock
    private CodeValueRepositoryWrapper codeValueRepository;
    @Mock
    private CashierTransactionDataValidator cashierTransactionDataValidator;
    @Mock
    private GLIMAccountInfoRepository glimRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private RepaymentWithPostDatedChecksAssembler repaymentWithPostDatedChecksAssembler;
    @Mock
    private PostDatedChecksRepository postDatedChecksRepository;
    @Mock
    private LoanRepaymentScheduleInstallmentRepository loanRepaymentScheduleInstallmentRepository;
    @Mock
    private LoanLifecycleStateMachine loanLifecycleStateMachine;
    @Mock
    private AccountLockService loanAccountLockService;
    @Mock
    private ExternalIdFactory externalIdFactory;
    @Mock
    private ReplayedTransactionBusinessEventService replayedTransactionBusinessEventService;
    @Mock
    private LoanAccrualTransactionBusinessEventService loanAccrualTransactionBusinessEventService;
    @Mock
    private ErrorHandler errorHandler;
    @Mock
    private LoanDownPaymentHandlerService loanDownPaymentHandlerService;
    @Mock
    private AccountTransferRepository accountTransferRepository;
    @Mock
    private LoanTransactionAssembler loanTransactionAssembler;
    @Mock
    private LoanAccrualsProcessingService loanAccrualsProcessingService;
    @Mock
    private LoanChargeValidator loanChargeValidator;
    @Mock
    private LoanJournalEntryPoster journalEntryPoster;

    @Test
    void givenMerchantIssuedRefundTransactionWithRelatedTransactions_whenAdjustExistingTransaction_thenRelatedTransactionsAreReversedAndEventsTriggered() {
        // Arrange
        Loan loan = mock(Loan.class);
        LoanTransaction transactionForAdjustment = mock(LoanTransaction.class);
        LoanTransaction newTransactionDetail = mock(LoanTransaction.class);
        ScheduleGeneratorDTO scheduleGeneratorDTO = mock(ScheduleGeneratorDTO.class);
        ExternalId reversalExternalId = ExternalId.generate();

        // Mock transaction type
        when(transactionForAdjustment.getTypeOf()).thenReturn(LoanTransactionType.MERCHANT_ISSUED_REFUND);
        when(transactionForAdjustment.isNotRepaymentLikeType()).thenReturn(false);

        // Mock transaction date
        when(transactionForAdjustment.getTransactionDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()));

        // Mock transaction ID to prevent NullPointerException
        when(transactionForAdjustment.getId()).thenReturn(1L);

        // Mock loan transactions
        LoanTransaction relatedTransaction = mock(LoanTransaction.class);
        when(relatedTransaction.isNotReversed()).thenReturn(true);

        LoanTransactionRelation transactionRelation = mock(LoanTransactionRelation.class);
        when(transactionRelation.getRelationType()).thenReturn(LoanTransactionRelationTypeEnum.RELATED);
        when(transactionRelation.getToTransaction()).thenReturn(transactionForAdjustment);

        Set<LoanTransactionRelation> transactionRelations = new HashSet<>();
        transactionRelations.add(transactionRelation);

        when(relatedTransaction.getLoanTransactionRelations()).thenReturn(transactionRelations);

        List<LoanTransaction> loanTransactions = Arrays.asList(transactionForAdjustment, relatedTransaction);
        when(loan.getLoanTransactions()).thenReturn(loanTransactions);

        doNothing().when(loanTransactionValidator).validateActivityNotBeforeClientOrGroupTransferDate(any(), any(), any());
        when(loan.isClosedWrittenOff()).thenReturn(false);
        when(newTransactionDetail.isRepaymentLikeType()).thenReturn(true);

        // Act
        underTest.adjustExistingTransaction(loan, newTransactionDetail, transactionForAdjustment, scheduleGeneratorDTO, reversalExternalId);

        // Assert
        // Verify that related transaction is reversed and event is triggered
        verify(relatedTransaction).reverse();
        verify(relatedTransaction).manuallyAdjustedOrReversed();

        ArgumentCaptor<LoanAdjustTransactionBusinessEvent> eventCaptor = ArgumentCaptor.forClass(LoanAdjustTransactionBusinessEvent.class);
        verify(businessEventNotifierService).notifyPostBusinessEvent(eventCaptor.capture());

        LoanAdjustTransactionBusinessEvent event = eventCaptor.getValue();
        assertEquals(relatedTransaction, event.get().getTransactionToAdjust());
    }

    @Test
    void givenNonMerchantIssuedRefundTransaction_whenAdjustExistingTransaction_thenNoRelatedTransactionsReversed() {
        // Arrange
        Loan loan = mock(Loan.class);
        LoanTransaction transactionForAdjustment = mock(LoanTransaction.class);
        LoanTransaction newTransactionDetail = mock(LoanTransaction.class);
        ScheduleGeneratorDTO scheduleGeneratorDTO = mock(ScheduleGeneratorDTO.class);
        ExternalId reversalExternalId = ExternalId.generate();

        // Mock transaction type
        when(transactionForAdjustment.getTypeOf()).thenReturn(LoanTransactionType.REPAYMENT);
        when(transactionForAdjustment.isNotRepaymentLikeType()).thenReturn(false);

        // Mock transaction date
        when(transactionForAdjustment.getTransactionDate()).thenReturn(LocalDate.now(ZoneId.systemDefault()));

        // Mock loan transactions
        LoanTransaction unrelatedTransaction = mock(LoanTransaction.class);

        // Mock methods called inside adjustExistingTransaction
        doNothing().when(loanTransactionValidator).validateActivityNotBeforeClientOrGroupTransferDate(any(), any(), any());
        when(loan.isClosedWrittenOff()).thenReturn(false);
        when(newTransactionDetail.isRepaymentLikeType()).thenReturn(true);

        // Act
        underTest.adjustExistingTransaction(loan, newTransactionDetail, transactionForAdjustment, scheduleGeneratorDTO, reversalExternalId);

        // Assert
        // Verify that no related transactions are reversed
        verify(unrelatedTransaction, never()).reverse();
        verify(unrelatedTransaction, never()).manuallyAdjustedOrReversed();
        verify(businessEventNotifierService, never()).notifyPostBusinessEvent(any(LoanAdjustTransactionBusinessEvent.class));
    }

}
