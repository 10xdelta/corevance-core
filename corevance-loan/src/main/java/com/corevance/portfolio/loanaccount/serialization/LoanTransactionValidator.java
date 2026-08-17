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
package com.corevance.portfolio.loanaccount.serialization;

import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.util.List;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.DataValidatorBuilder;
import com.corevance.organisation.holiday.domain.Holiday;
import com.corevance.organisation.workingdays.domain.WorkingDays;
import com.corevance.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanDisbursementDetails;
import com.corevance.portfolio.loanaccount.domain.LoanEvent;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionType;

public interface LoanTransactionValidator {

    void validateDisbursement(JsonCommand command, boolean isAccountTransfer, Long loanId);

    void validateUndoChargeOff(String json);

    void validateTransaction(String json);

    default void validateTransaction(Loan loan, LoanTransactionType loanTransactionType, String json) {
        validateTransaction(json);
        validateLoanNotClosedOrOverpaidForTransactions(loan, loanTransactionType);
    }

    void validateChargebackTransaction(String json);

    void validateNewRepaymentTransaction(String json);

    void validateTransactionWithNoAmount(String json);

    void validateChargeOffTransaction(String json);

    void validateUpdateOfLoanOfficer(String json);

    void validateForBulkLoanReassignment(String json);

    void validateMarkAsFraudLoan(String json);

    void validateUpdateDisbursementDateAndAmount(String json, LoanDisbursementDetails loanDisbursementDetails);

    void validateNewRefundTransaction(String json);

    void validateLoanForeclosure(String json);

    void validateLoanClientIsActive(Loan loan);

    void validateLoanGroupIsActive(Loan loan);

    void validateActivityNotBeforeLastTransactionDate(Loan loan, LocalDate activityDate, LoanEvent event);

    void validateRepaymentDateIsOnNonWorkingDay(LocalDate repaymentDate, WorkingDays workingDays, boolean allowTransactionsOnNonWorkingDay);

    void validateRepaymentDateIsOnHoliday(LocalDate repaymentDate, boolean allowTransactionsOnHoliday, List<Holiday> holidays);

    void validateLoanTransactionInterestPaymentWaiver(JsonCommand command);

    void validateLoanTransactionInterestPaymentWaiverAfterRecalculation(Loan loan);

    void validateRefund(String json);

    void validateRefund(Loan loan, LoanTransactionType loanTransactionType, LocalDate transactionDate,
            ScheduleGeneratorDTO scheduleGeneratorDTO);

    void validateRefundDateIsAfterLastRepayment(Loan loan, LocalDate refundTransactionDate);

    void validateActivityNotBeforeClientOrGroupTransferDate(Loan loan, LoanEvent event, LocalDate activityDate);

    void validatePaymentDetails(DataValidatorBuilder baseDataValidator, JsonElement element);

    void validateIfTransactionIsChargeback(LoanTransaction chargebackTransaction);

    void validateLoanRescheduleDate(Loan loan);

    void validateNote(DataValidatorBuilder baseDataValidator, JsonElement element);

    void validateExternalId(DataValidatorBuilder baseDataValidator, JsonElement element);

    void validateReversalExternalId(DataValidatorBuilder baseDataValidator, JsonElement element);

    void validateManualInterestRefundTransaction(String json);

    void validateClassificationCodeValue(String codeName, Long transactionClassificationId, DataValidatorBuilder baseDataValidator);

    void validateLoanNotClosedOrOverpaidForTransactions(Loan loan);

    void validateLoanNotClosedOrOverpaidForTransactions(Loan loan, LoanTransactionType loanTransactionType);
}
