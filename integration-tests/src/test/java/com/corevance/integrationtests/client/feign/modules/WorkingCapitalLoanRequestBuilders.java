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
package com.corevance.integrationtests.client.feign.modules;

import java.math.BigDecimal;
import com.corevance.client.models.ChargeRequest;
import com.corevance.client.models.ExecuteWorkingCapitalLoanTransactionCommandRequest;
import com.corevance.client.models.PostLoansLoanIdChargesRequest;
import com.corevance.client.models.PostWorkingCapitalLoanTransactionsRequest;
import com.corevance.client.models.PostWorkingCapitalLoansLoanIdChargesChargeIdRequest;
import com.corevance.client.models.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest;
import com.corevance.client.models.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.NearBreachFrequencyTypeEnum;
import com.corevance.client.models.PostWorkingCapitalLoansLoanIdRequest;
import com.corevance.client.models.PostWorkingCapitalLoansRequest;
import com.corevance.client.models.PutWorkingCapitalLoansLoanIdRateRequest;
import com.corevance.integrationtests.common.Utils;

public final class WorkingCapitalLoanRequestBuilders {

    private static final String LOCALE = "en";
    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private static final Integer CHARGE_APPLIES_TO_WORKING_CAPITAL_LOAN = 5;
    private static final Integer CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE = 2;
    private static final Integer CHARGE_CALCULATION_TYPE_FLAT = 1;
    private static final String CHARGE_CURRENCY_CODE = "USD";

    private WorkingCapitalLoanRequestBuilders() {}

    public static PostWorkingCapitalLoansRequest submitApplication(Long clientId, Long productId, BigDecimal principal,
            BigDecimal periodPaymentRate, String submittedOnDate, String expectedDisbursementDate) {
        return new PostWorkingCapitalLoansRequest().clientId(clientId).productId(productId).principalAmount(principal)
                .periodPaymentRate(periodPaymentRate).submittedOnDate(submittedOnDate).expectedDisbursementDate(expectedDisbursementDate)
                .totalPaymentVolume(BigDecimal.valueOf(100000)).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest approve(String approvedOnDate, BigDecimal approvedAmount,
            String expectedDisbursementDate) {
        return new PostWorkingCapitalLoansLoanIdRequest().approvedOnDate(approvedOnDate).approvedLoanAmount(approvedAmount)
                .expectedDisbursementDate(expectedDisbursementDate).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest approveWithDiscount(String approvedOnDate, BigDecimal approvedAmount,
            String expectedDisbursementDate, BigDecimal discountAmount) {
        return new PostWorkingCapitalLoansLoanIdRequest().approvedOnDate(approvedOnDate).approvedLoanAmount(approvedAmount)
                .expectedDisbursementDate(expectedDisbursementDate).discountAmount(discountAmount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest disburse(String actualDisbursementDate, BigDecimal transactionAmount) {
        return new PostWorkingCapitalLoansLoanIdRequest().actualDisbursementDate(actualDisbursementDate)
                .transactionAmount(transactionAmount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest disburseWithDiscount(String actualDisbursementDate, BigDecimal transactionAmount,
            BigDecimal discountAmount) {
        return new PostWorkingCapitalLoansLoanIdRequest().actualDisbursementDate(actualDisbursementDate)
                .transactionAmount(transactionAmount).discountAmount(discountAmount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest undoDisbursal() {
        return new PostWorkingCapitalLoansLoanIdRequest().locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdRequest emptyCommand() {
        return new PostWorkingCapitalLoansLoanIdRequest().locale(LOCALE).dateFormat(DATE_FORMAT);
    }

    public static PutWorkingCapitalLoansLoanIdRateRequest updateRate(BigDecimal newRate) {
        return new PutWorkingCapitalLoansLoanIdRateRequest().periodPaymentRate(newRate).locale(LOCALE);
    }

    public static PostWorkingCapitalLoansLoanIdNearBreachActionsRequest createNearBreachRescheduleAction(BigDecimal threshold,
            Integer frequency, String frequencyType) {
        return new PostWorkingCapitalLoansLoanIdNearBreachActionsRequest()
                .action(PostWorkingCapitalLoansLoanIdNearBreachActionsRequest.ActionEnum.RESCHEDULE).nearBreachThreshold(threshold)
                .nearBreachFrequency(frequency).nearBreachFrequencyType(NearBreachFrequencyTypeEnum.fromValue(frequencyType))
                .locale(LOCALE);
    }

    public static PostWorkingCapitalLoanTransactionsRequest repayment(BigDecimal amount, String transactionDate) {
        return new PostWorkingCapitalLoanTransactionsRequest().transactionAmount(amount).transactionDate(transactionDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoanTransactionsRequest goodwillCredit(BigDecimal amount, String transactionDate) {
        return new PostWorkingCapitalLoanTransactionsRequest().transactionAmount(amount).transactionDate(transactionDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static ExecuteWorkingCapitalLoanTransactionCommandRequest undoTransaction() {
        return new ExecuteWorkingCapitalLoanTransactionCommandRequest();
    }

    public static ChargeRequest specifiedDueDateCharge(boolean penalty, double amount) {
        return new ChargeRequest().chargeAppliesTo(CHARGE_APPLIES_TO_WORKING_CAPITAL_LOAN)
                .chargeTimeType(CHARGE_TIME_TYPE_SPECIFIED_DUE_DATE).chargeCalculationType(CHARGE_CALCULATION_TYPE_FLAT)
                .name(Utils.uniqueRandomStringGenerator("WCL_CHARGE_", 8)).amount(amount).active(true).currencyCode(CHARGE_CURRENCY_CODE)
                .locale(LOCALE).penalty(penalty);
    }

    public static PostLoansLoanIdChargesRequest addCharge(Long chargeId, double amount, String dueDate) {
        return new PostLoansLoanIdChargesRequest().chargeId(chargeId).amount(amount).dueDate(dueDate).locale(LOCALE)
                .dateFormat(DATE_FORMAT);
    }

    public static PostWorkingCapitalLoansLoanIdChargesChargeIdRequest chargeAdjustment(BigDecimal amount) {
        return new PostWorkingCapitalLoansLoanIdChargesChargeIdRequest().amount(amount).locale(LOCALE).dateFormat(DATE_FORMAT);
    }
}
