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
package com.corevance.test.stepdef.loan;

import static com.corevance.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.GetLoansLoanIdResponse;
import com.corevance.client.models.PostClientsResponse;
import com.corevance.client.models.PostLoansRequest;
import com.corevance.client.models.PostLoansResponse;
import com.corevance.client.models.PutLoansLoanIdRequest;
import com.corevance.client.models.PutLoansLoanIdResponse;
import com.corevance.test.data.loanproduct.DefaultLoanProduct;
import com.corevance.test.data.loanproduct.LoanProductResolver;
import com.corevance.test.factory.LoanRequestFactory;
import com.corevance.test.stepdef.AbstractStepDef;
import com.corevance.test.support.TestContextKey;

@RequiredArgsConstructor
public class LoanOverrideFieldsStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final CorevanceFeignClient corevanceClient;
    private final LoanRequestFactory loanRequestFactory;
    private final LoanProductResolver loanProductResolver;

    @Then("LoanDetails has {string} field with value: {string}")
    public void checkLoanDetailsFieldWithValue(final String fieldName, final String expectedValue) throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse);
        final Long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetails = ok(
                () -> corevanceClient.loans().retrieveOneLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));

        assertNotNull(loanDetails);

        verifyFieldValue(loanDetails, fieldName, expectedValue);
    }

    private void verifyFieldValue(final GetLoansLoanIdResponse loanDetails, final String fieldName, final String expectedValue) {
        final Integer actualValue = getIntFieldValue(loanDetails, fieldName);
        final Integer expected = Integer.valueOf(expectedValue);
        assertThat(actualValue).as("Expected %s to be %d but was %s", fieldName, expected, actualValue).isEqualTo(expected);
    }

    private Integer getIntFieldValue(final GetLoansLoanIdResponse loanDetails, final String fieldName) {
        return switch (fieldName) {
            case "inArrearsTolerance" -> loanDetails.getInArrearsTolerance();
            case "graceOnPrincipalPayment" -> loanDetails.getGraceOnPrincipalPayment();
            case "graceOnInterestPayment" -> loanDetails.getGraceOnInterestPayment();
            case "graceOnArrearsAgeing" -> loanDetails.getGraceOnArrearsAgeing();
            case "interestType" -> loanDetails.getInterestType().getId().intValue();
            case "amortizationType" -> loanDetails.getAmortizationType().getId().intValue();
            case "interestCalculationPeriodType" -> loanDetails.getInterestCalculationPeriodType().getId().intValue();
            case "repaymentEvery" -> loanDetails.getRepaymentEvery();
            case "principal" -> loanDetails.getPrincipal().intValue();
            default -> throw new IllegalArgumentException("Unknown override field: " + fieldName);
        };
    }

    @When("Admin creates a new Loan with the following override data:")
    public void createLoanWithOverrideData(final DataTable dataTable) throws IOException {
        final PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        assertNotNull(clientResponse);
        final Long clientId = clientResponse.getClientId();

        final Map<String, String> overrideData = dataTable.asMap(String.class, String.class);

        final String loanProductName = overrideData.get("loanProduct");
        if (loanProductName == null) {
            throw new IllegalArgumentException("loanProduct is required in override data");
        }

        final PostLoansRequest loansRequest = loanRequestFactory.defaultLoansRequest(clientId)
                .productId(loanProductResolver.resolve(DefaultLoanProduct.valueOf(loanProductName))).numberOfRepayments(6)
                .loanTermFrequency(180).interestRatePerPeriod(new BigDecimal(1));

        overrideData.forEach((fieldName, value) -> {
            if (!"loanProduct".equals(fieldName)) {
                applyOverrideField(loansRequest, fieldName, value);
            }
        });

        final PostLoansResponse response = ok(() -> corevanceClient.loans().calculateOrSubmitLoanApplication(loansRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, response);

    }

    private void applyOverrideField(final PostLoansRequest request, final String fieldName, final String value) {
        final boolean isNull = "null".equals(value);

        switch (fieldName) {
            case "inArrearsTolerance" -> request.inArrearsTolerance(isNull ? null : new BigDecimal(value));
            case "graceOnInterestPayment" -> request.graceOnInterestPayment(isNull ? null : Integer.valueOf(value));
            case "graceOnPrincipalPayment" -> request.graceOnPrincipalPayment(isNull ? null : Integer.valueOf(value));
            case "graceOnArrearsAgeing" -> request.graceOnArrearsAgeing(isNull ? null : Integer.valueOf(value));
            case "interestType" -> request.interestType(isNull ? null : Integer.valueOf(value));
            case "amortizationType" -> request.amortizationType(isNull ? null : Integer.valueOf(value));
            case "interestCalculationPeriodType" -> request.interestCalculationPeriodType(isNull ? null : Integer.valueOf(value));
            case "repaymentEvery" -> request.repaymentEvery(isNull ? null : Integer.valueOf(value));
            default -> throw new IllegalArgumentException("Unknown override field: " + fieldName);
        }
    }

    @When("Admin modifies the loan, changing principal to {string} and omitting the override-enabled schedule fields")
    public void modifyLoanOmittingOverrideEnabledScheduleFields(final String newPrincipal) throws IOException {
        modifyLoanPrincipal(newPrincipal, null);
    }

    @When("Admin modifies the loan, changing principal to {string} and setting interestType to {string}")
    public void modifyLoanSettingInterestType(final String newPrincipal, final String interestType) throws IOException {
        modifyLoanPrincipal(newPrincipal, Integer.valueOf(interestType));
    }

    /**
     * Modifies the loan application, always changing the principal so that the schedule has to be recalculated.
     * {@code interestType} is only sent when explicitly given; when it is {@code null} the parameter is left out of the
     * request entirely, together with the other override-enabled schedule fields (amortizationType,
     * interestCalculationPeriodType, repaymentEvery). Those fields are optional on modify, so omitting them must leave
     * the loan's existing values untouched rather than fail.
     */
    private void modifyLoanPrincipal(final String newPrincipal, final Integer interestType) throws IOException {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        assertNotNull(loanResponse);
        final Long loanId = loanResponse.getLoanId();

        final GetLoansLoanIdResponse loanDetails = ok(
                () -> corevanceClient.loans().retrieveOneLoan(loanId, Map.of("staffInSelectedOfficeOnly", "false")));
        assertNotNull(loanDetails);

        final PutLoansLoanIdRequest modifyRequest = new PutLoansLoanIdRequest()//
                .productId(loanDetails.getLoanProductId())//
                .clientId(loanDetails.getClientId())//
                .principal(Long.valueOf(newPrincipal))//
                .loanTermFrequency(loanDetails.getTermFrequency())//
                .loanTermFrequencyType(loanDetails.getTermPeriodFrequencyType().getId().intValue())//
                .numberOfRepayments(loanDetails.getNumberOfRepayments())//
                .repaymentFrequencyType(loanDetails.getRepaymentFrequencyType().getId().intValue())//
                .interestRatePerPeriod(loanDetails.getInterestRatePerPeriod())//
                .expectedDisbursementDate(FORMATTER.format(loanDetails.getTimeline().getExpectedDisbursementDate()))//
                .submittedOnDate(FORMATTER.format(loanDetails.getTimeline().getSubmittedOnDate()))//
                .dateFormat(DATE_FORMAT)//
                .locale("en")//
                .loanType("individual");//

        if (interestType != null) {
            modifyRequest.interestType(interestType);
        }

        final PutLoansLoanIdResponse modifyResponse = ok(
                () -> corevanceClient.loans().updateLoanApplication(loanId, modifyRequest, Map.of()));
        testContext().set(TestContextKey.LOAN_MODIFY_RESPONSE, modifyResponse);
    }

}
