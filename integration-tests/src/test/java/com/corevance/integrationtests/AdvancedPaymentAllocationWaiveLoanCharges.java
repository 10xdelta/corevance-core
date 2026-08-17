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
package com.corevance.integrationtests;

import static com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.PhasedSettlementScheduleProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.models.AdvancedPaymentData;
import com.corevance.client.models.GetLoansLoanIdResponse;
import com.corevance.client.models.GetLoansLoanIdTransactions;
import com.corevance.client.models.PaymentAllocationOrder;
import com.corevance.client.models.PostLoanProductsRequest;
import com.corevance.client.models.PostLoansLoanIdChargesChargeIdRequest;
import com.corevance.integrationtests.client.feign.FeignLoanTestBase;
import com.corevance.integrationtests.common.Utils;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import com.corevance.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class AdvancedPaymentAllocationWaiveLoanCharges extends FeignLoanTestBase {

    @Test
    public void testAddFeeAndWaiveAdvancedPaymentAllocationNoBackdated() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = createClient();
            // Create Loan Product
            Long loanProductId = createLoanProductWithAdvancedAllocation();
            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0, 1,
                    (req) -> req.transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                            .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()));
            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");
            // Add Penalty
            Long loanChargeId = addCharge(loanId, false, 50, "01 January 2023");
            // When Waive Created Penalty
            waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

            // Then verify
            verifyTransactions(loanId, //
                    transaction(1000, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(50, "Waive loan charges", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0) //
            );

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            GetLoansLoanIdTransactions waiveTransaction = loanDetails.getTransactions().get(1);
            Assertions.assertNotNull(waiveTransaction.getLoanChargePaidByList());
            Assertions.assertEquals(1, waiveTransaction.getLoanChargePaidByList().size());
            Assertions.assertEquals(loanChargeId, waiveTransaction.getLoanChargePaidByList().get(0).getChargeId());
            Assertions.assertEquals(50.0, Utils.getDoubleValue(waiveTransaction.getLoanChargePaidByList().get(0).getAmount()));
        });
    }

    @Test
    public void testAddPenaltyAndWaiveAdvancedPaymentAllocationNoBackDated() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = createClient();
            // Create Loan Product
            Long loanProductId = createLoanProductWithAdvancedAllocation();
            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0, 1,
                    (req) -> req.transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                            .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString()));
            // Disburse Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");
            // Add Penalty
            Long loanChargeId = addCharge(loanId, true, 50, "01 January 2023");
            // When Waive Created Penalty
            waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

            // Then verify
            verifyTransactions(loanId, //
                    transaction(1000, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(50, "Waive loan charges", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0) //
            );

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            GetLoansLoanIdTransactions waiveTransaction = loanDetails.getTransactions().get(1);
            Assertions.assertNotNull(waiveTransaction.getLoanChargePaidByList());
            Assertions.assertEquals(1, waiveTransaction.getLoanChargePaidByList().size());
            Assertions.assertEquals(loanChargeId, waiveTransaction.getLoanChargePaidByList().get(0).getChargeId());
            Assertions.assertEquals(50.0, Utils.getDoubleValue(waiveTransaction.getLoanChargePaidByList().get(0).getAmount()));
        });
    }

    @Test
    public void testAddPenaltyAndWaiveAdvancedPaymentAllocationAndBackdatedRepayment() {
        runAt("01 January 2023", () -> {
            // Create Client
            Long clientId = createClient();
            // Create Loan Product
            Long loanProductId = createLoanProductWithAdvancedAllocation();
            // Apply and Approve Loan
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2023", 1000.0, 1,
                    (req) -> req.transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                            .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString())); // Disburse
                                                                                                            // Loan
            disburseLoan(loanId, BigDecimal.valueOf(1000.00), "01 January 2023");

            // set business date to
            updateBusinessDate("05 January 2023");

            // Add Penalty
            Long loanChargeId = addCharge(loanId, true, 50, "05 January 2023");

            // When Waive Created Penalty
            waiveLoanCharge(loanId, loanChargeId, new PostLoansLoanIdChargesChargeIdRequest());

            // Then verify
            verifyTransactions(loanId, //
                    transaction(1000, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(50, "Waive loan charges", "05 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0) //
            );

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            GetLoansLoanIdTransactions waiveTransaction = loanDetails.getTransactions().get(1);
            Assertions.assertNotNull(waiveTransaction.getLoanChargePaidByList());
            Assertions.assertEquals(1, waiveTransaction.getLoanChargePaidByList().size());
            Assertions.assertEquals(loanChargeId, waiveTransaction.getLoanChargePaidByList().get(0).getChargeId());
            Assertions.assertEquals(50.0, Utils.getDoubleValue(waiveTransaction.getLoanChargePaidByList().get(0).getAmount()));

            addRepaymentForLoan(loanId, 200.0, "03 January 2023");

            verifyTransactions(loanId, //
                    transaction(1000, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(200, "Repayment", "03 January 2023", 800.0, 200.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(50, "Waive loan charges", "05 January 2023", 800.0, 0.0, 0.0, 0.0, 0.0, 50.0, 0.0) //
            );
        });
    }

    private AdvancedPaymentData createDefaultPaymentAllocationWithMixedGrouping() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

    protected Long createLoanProductWithAdvancedAllocation() {
        PostLoanProductsRequest req = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct();
        req.transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).loanScheduleType(LoanScheduleType.PROGRESSIVE.name())
                .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.toString());
        req.addPaymentAllocationItem(createDefaultPaymentAllocationWithMixedGrouping());
        return createLoanProduct(req);
    }

}
