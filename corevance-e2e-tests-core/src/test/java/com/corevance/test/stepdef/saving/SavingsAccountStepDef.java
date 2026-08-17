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
package com.corevance.test.stepdef.saving;

import static com.corevance.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.PostClientsResponse;
import com.corevance.client.models.PostSavingsAccountTransactionsRequest;
import com.corevance.client.models.PostSavingsAccountTransactionsResponse;
import com.corevance.client.models.PostSavingsAccountsAccountIdRequest;
import com.corevance.client.models.PostSavingsAccountsAccountIdResponse;
import com.corevance.client.models.PostSavingsAccountsRequest;
import com.corevance.client.models.PostSavingsAccountsResponse;
import com.corevance.client.models.PostSavingsProductsRequest;
import com.corevance.client.models.PostSavingsProductsResponse;
import com.corevance.client.models.SavingsAccountData;
import com.corevance.client.models.SavingsAccountTransactionData;
import com.corevance.test.factory.SavingsAccountRequestFactory;
import com.corevance.test.factory.SavingsProductRequestFactory;
import com.corevance.test.helper.ErrorMessageHelper;
import com.corevance.test.helper.ErrorResponse;
import com.corevance.test.helper.Utils;
import com.corevance.test.stepdef.AbstractStepDef;
import com.corevance.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

public class SavingsAccountStepDef extends AbstractStepDef {

    @Autowired
    private CorevanceFeignClient corevanceClient;

    public static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final String EUR = "EUR";

    @And("Admin creates a EUR savings product")
    public void createEurSavingsProduct() {
        PostSavingsProductsRequest savingsProductRequest = SavingsProductRequestFactory.defaultSavingsProductRequest();
        PostSavingsProductsResponse savingsProductResponse = ok(
                () -> corevanceClient.savingsProduct().createSavingsProduct(savingsProductRequest));
        testContext().set(TestContextKey.DEFAULT_SAVINGS_PRODUCT_CREATE_RESPONSE_EUR, savingsProductResponse);
    }

    @And("Client creates a new EUR savings account with {string} submitted on date")
    public void createSavingsAccountEUR(String submittedOnDate) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        long clientId = clientResponse.getClientId();

        PostSavingsProductsResponse savingsProductResponse = testContext().get(TestContextKey.DEFAULT_SAVINGS_PRODUCT_CREATE_RESPONSE_EUR);
        long productId = savingsProductResponse.getResourceId();

        PostSavingsAccountsRequest createSavingsAccountRequest = SavingsAccountRequestFactory.defaultEURSavingsAccountRequest()
                .clientId(clientId).productId(productId).submittedOnDate(submittedOnDate);

        PostSavingsAccountsResponse createSavingsAccountResponse = ok(
                () -> corevanceClient.savingsAccount().submitSavingsApplication(createSavingsAccountRequest));
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE, createSavingsAccountResponse);
        testContext().set(TestContextKey.LAST_SAVINGS_ACCOUNT_ID, createSavingsAccountResponse.getSavingsId());
    }

    @And("Client creates a new USD savings account with {string} submitted on date")
    public void createSavingsAccountUSD(String submittedOnDate) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        long clientId = clientResponse.getClientId();

        PostSavingsAccountsRequest createSavingsAccountRequest = SavingsAccountRequestFactory.defaultUSDSavingsAccountRequest()
                .clientId(clientId).submittedOnDate(submittedOnDate);

        PostSavingsAccountsResponse createSavingsAccountResponse = ok(
                () -> corevanceClient.savingsAccount().submitSavingsApplication(createSavingsAccountRequest));
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE, createSavingsAccountResponse);
        testContext().set(TestContextKey.LAST_SAVINGS_ACCOUNT_ID, createSavingsAccountResponse.getSavingsId());
    }

    @And("Client creates a {string} new EUR savings account with {string} submitted on date")
    public void createNamedSavingsAccountEUR(String accountAlias, String submittedOnDate) {
        PostSavingsAccountsResponse response = createSavingsAccount(submittedOnDate);
        testContext().set(namedSavingsAccountCreateKey(EUR, accountAlias), response);
        testContext().set(TestContextKey.LAST_SAVINGS_ACCOUNT_ID, response.getSavingsId());
    }

    @And("Approve EUR savings account on {string} date")
    public void approveEurSavingsAccount(String approvedOnDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountsAccountIdRequest approveSavingsAccountRequest = SavingsAccountRequestFactory.defaultApproveRequest()
                .approvedOnDate(approvedOnDate);

        PostSavingsAccountsAccountIdResponse approveSavingsAccountResponse = ok(() -> corevanceClient.savingsAccount()
                .handleCommandsSavingsAccount(savingsAccountID, approveSavingsAccountRequest, Map.of("command", "approve")));
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_APPROVE_RESPONSE, approveSavingsAccountResponse);
    }

    @And("Approve USD savings account on {string} date")
    public void approveUsdSavingsAccount(String approvedOnDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountsAccountIdRequest approveSavingsAccountRequest = SavingsAccountRequestFactory.defaultApproveRequest()
                .approvedOnDate(approvedOnDate);

        PostSavingsAccountsAccountIdResponse approveSavingsAccountResponse = ok(() -> corevanceClient.savingsAccount()
                .handleCommandsSavingsAccount(savingsAccountID, approveSavingsAccountRequest, Map.of("command", "approve")));
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_APPROVE_RESPONSE, approveSavingsAccountResponse);
    }

    @And("Approve {string} EUR savings account on {string} date")
    public void approveNamedEurSavingsAccount(String accountAlias, String approvedOnDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(namedSavingsAccountCreateKey(EUR, accountAlias));
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountsAccountIdRequest approveSavingsAccountRequest = SavingsAccountRequestFactory.defaultApproveRequest()
                .approvedOnDate(approvedOnDate);

        PostSavingsAccountsAccountIdResponse approveSavingsAccountResponse = ok(() -> corevanceClient.savingsAccount()
                .handleCommandsSavingsAccount(savingsAccountID, approveSavingsAccountRequest, Map.of("command", "approve")));
        testContext().set(namedSavingsAccountApproveKey(EUR, accountAlias), approveSavingsAccountResponse);
    }

    @And("Activate EUR savings account on {string} date")
    public void activateSavingsAccount(String activatedOnDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountsAccountIdRequest activateSavingsAccountRequest = SavingsAccountRequestFactory.defaultActivateRequest()
                .activatedOnDate(activatedOnDate);

        PostSavingsAccountsAccountIdResponse activateSavingsAccountResponse = ok(() -> corevanceClient.savingsAccount()
                .handleCommandsSavingsAccount(savingsAccountID, activateSavingsAccountRequest, Map.of("command", "activate")));
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_ACTIVATED_RESPONSE, activateSavingsAccountResponse);
    }

    @And("Activate USD savings account on {string} date")
    public void activateUsdSavingsAccount(String activatedOnDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountsAccountIdRequest activateSavingsAccountRequest = SavingsAccountRequestFactory.defaultActivateRequest()
                .activatedOnDate(activatedOnDate);

        PostSavingsAccountsAccountIdResponse activateSavingsAccountResponse = ok(() -> corevanceClient.savingsAccount()
                .handleCommandsSavingsAccount(savingsAccountID, activateSavingsAccountRequest, Map.of("command", "activate")));
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_ACTIVATED_RESPONSE, activateSavingsAccountResponse);
    }

    @And("Activate {string} EUR savings account on {string} date")
    public void activateNamedEurSavingsAccount(String accountAlias, String activatedOnDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(namedSavingsAccountCreateKey(EUR, accountAlias));
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountsAccountIdRequest activateSavingsAccountRequest = SavingsAccountRequestFactory.defaultActivateRequest()
                .activatedOnDate(activatedOnDate);

        PostSavingsAccountsAccountIdResponse activateSavingsAccountResponse = ok(() -> corevanceClient.savingsAccount()
                .handleCommandsSavingsAccount(savingsAccountID, activateSavingsAccountRequest, Map.of("command", "activate")));
        testContext().set(namedSavingsAccountActivateKey(EUR, accountAlias), activateSavingsAccountResponse);
    }

    @And("Client successfully deposits {double} EUR to the savings account on {string} date")
    public void createEurDeposit(double depositAmount, String depositDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountTransactionsRequest depositRequest = SavingsAccountRequestFactory.defaultDepositRequest()
                .transactionDate(depositDate).transactionAmount(BigDecimal.valueOf(depositAmount));

        PostSavingsAccountTransactionsResponse depositResponse = ok(() -> corevanceClient.savingsAccountTransactions()
                .createSavingsAccountTransaction(savingsAccountID, depositRequest, Map.of("command", "deposit")));
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_DEPOSIT_RESPONSE, depositResponse);
    }

    @And("Client successfully deposits {double} USD to the savings account on {string} date")
    public void createUsdDeposit(double depositAmount, String depositDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountTransactionsRequest depositRequest = SavingsAccountRequestFactory.defaultDepositRequest()
                .transactionDate(depositDate).transactionAmount(BigDecimal.valueOf(depositAmount));

        PostSavingsAccountTransactionsResponse depositResponse = ok(() -> corevanceClient.savingsAccountTransactions()
                .createSavingsAccountTransaction(savingsAccountID, depositRequest, Map.of("command", "deposit")));
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_DEPOSIT_RESPONSE, depositResponse);
    }

    @And("Client successfully withdraw {double} EUR from the savings account on {string} date")
    public void createEurWithdraw(double withdrawAmount, String transcationDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountTransactionsRequest withdrawRequest = SavingsAccountRequestFactory.defaultWithdrawRequest()
                .transactionDate(transcationDate).transactionAmount(BigDecimal.valueOf(withdrawAmount));

        PostSavingsAccountTransactionsResponse withdrawalResponse = ok(() -> corevanceClient.savingsAccountTransactions()
                .createSavingsAccountTransaction(savingsAccountID, withdrawRequest, Map.of("command", "withdrawal")));
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_WITHDRAW_RESPONSE, withdrawalResponse);
    }

    @And("Client successfully withdraw {double} USD from the savings account on {string} date")
    public void createUsdWithdraw(double withdrawAmount, String transcationDate) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountTransactionsRequest withdrawRequest = SavingsAccountRequestFactory.defaultWithdrawRequest()
                .transactionDate(transcationDate).transactionAmount(BigDecimal.valueOf(withdrawAmount));

        PostSavingsAccountTransactionsResponse withdrawalResponse = ok(() -> corevanceClient.savingsAccountTransactions()
                .createSavingsAccountTransaction(savingsAccountID, withdrawRequest, Map.of("command", "withdrawal")));
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_WITHDRAW_RESPONSE, withdrawalResponse);
    }

    @And("Client successfully deposits {double} EUR to the {string} savings account on {string} date")
    public void createNamedEurDeposit(double depositAmount, String accountAlias, String depositDate) {
        long savingsAccountID = getNamedSavingsAccountId(EUR, accountAlias);

        PostSavingsAccountTransactionsRequest depositRequest = SavingsAccountRequestFactory.defaultDepositRequest()
                .transactionDate(depositDate).transactionAmount(BigDecimal.valueOf(depositAmount));

        PostSavingsAccountTransactionsResponse depositResponse = ok(() -> corevanceClient.savingsAccountTransactions()
                .createSavingsAccountTransaction(savingsAccountID, depositRequest, Map.of("command", "deposit")));
        testContext().set(namedSavingsAccountDepositKey(EUR, accountAlias), depositResponse);
    }

    @And("Client tries to withdraw {double} {string} from savings account on {string} date and expects an error")
    public void withdrawWithInsufficientBalance(double withdrawAmount, String currency, String transactionDate) {

        String contextKey = currency.equalsIgnoreCase("EUR") ? TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE
                : TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE;

        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(contextKey);
        long savingsAccountID = savingsAccountResponse.getSavingsId();

        PostSavingsAccountTransactionsRequest withdrawRequest = SavingsAccountRequestFactory.defaultWithdrawRequest()
                .transactionDate(transactionDate).transactionAmount(BigDecimal.valueOf(withdrawAmount));

        try {
            ok(() -> corevanceClient.savingsAccountTransactions().createSavingsAccountTransaction(savingsAccountID, withdrawRequest,
                    "withdrawal"));

            fail("Expected withdrawal to fail due to insufficient funds, but it succeeded.");

        } catch (com.corevance.client.feign.util.CallFailedRuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.fromFeignException((com.corevance.client.feign.FeignException) e.getCause());
            testContext().set(TestContextKey.ERROR_RESPONSE, errorResponse);
        }
    }

    @Then("The savings account withdrawal error response has an HTTP status of {int}")
    public void verifySavingsWithdrawalHttpStatus(int expectedStatus) {
        ErrorResponse errorResponse = testContext().get(TestContextKey.ERROR_RESPONSE);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getHttpStatusCode()).isEqualTo(expectedStatus);
    }

    @And("The savings account withdrawal developer message contains {string}")
    public void verifySavingsWithdrawalErrorMessage(String expectedMessage) {
        ErrorResponse errorResponse = testContext().get(TestContextKey.ERROR_RESPONSE);
        assertThat(errorResponse).isNotNull();
        String developerMessage = errorResponse.getErrors().get(0).getDeveloperMessage();
        assertThat(developerMessage).contains(expectedMessage);
    }

    @And("Savings Transactions tab has the following data:")
    public void savingsTransactionsTabCheck(DataTable table) {
        Long savingsAccountId = testContext().get(TestContextKey.LAST_SAVINGS_ACCOUNT_ID);

        SavingsAccountData savingsAccountData = ok(
                () -> corevanceClient.savingsAccount().retrieveSavingsAccount(savingsAccountId, Map.of("associations", "transactions")));
        List<SavingsAccountTransactionData> transactions = savingsAccountData.getTransactions();
        List<List<String>> data = table.asLists();
        List<String> header = table.row(0);
        String resourceId = String.valueOf(savingsAccountId);
        checkSavingsTransactions(data, transactions, header, resourceId);
    }

    @And("{string} Savings Transactions tab has the following data:")
    public void namedSavingsTransactionsTabCheck(String accountAlias, DataTable table) {
        Long savingsAccountId = getNamedSavingsAccountId(EUR, accountAlias);

        SavingsAccountData savingsAccountData = ok(
                () -> corevanceClient.savingsAccount().retrieveSavingsAccount(savingsAccountId, Map.of("associations", "transactions")));
        List<SavingsAccountTransactionData> transactions = savingsAccountData.getTransactions();
        List<List<String>> data = table.asLists();
        List<String> header = table.row(0);
        String resourceId = String.valueOf(savingsAccountId);
        checkSavingsTransactions(data, transactions, header, resourceId);
    }

    public void checkSavingsTransactions(List<List<String>> data, List<SavingsAccountTransactionData> transactions, List<String> header,
            String resourceId) {
        checkLoanTransaction(data, transactions, header, resourceId);
        assertThat(transactions.size())
                .as(ErrorMessageHelper.nrOfLinesWrongInTransactionsTab(String.valueOf(resourceId), transactions.size(), data.size() - 1))
                .isEqualTo(data.size() - 1);
    }

    public void checkLoanTransaction(List<List<String>> data, List<SavingsAccountTransactionData> transactions, List<String> header,
            String resourceId) {
        for (int i = 1; i < data.size(); i++) {
            List<String> expectedValues = data.get(i);
            String transactionDateExpected = expectedValues.getFirst();
            List<List<String>> actualValuesList = transactions.stream()//
                    .filter(t -> transactionDateExpected.equals(FORMATTER.format(t.getDate())))//
                    .map(t -> fetchValuesOfTransaction(header, t))//
                    .collect(Collectors.toList());//
            boolean containsExpectedValues = actualValuesList.stream()//
                    .anyMatch(actualValues -> actualValues.equals(expectedValues));//
            assertThat(containsExpectedValues)
                    .as(ErrorMessageHelper.wrongValueInLineInTransactionsTab(resourceId, i, actualValuesList, expectedValues)).isTrue();
        }
    }

    private List<String> fetchValuesOfTransaction(List<String> header, SavingsAccountTransactionData t) {
        List<String> actualValues = new ArrayList<>();
        for (String headerName : header) {
            switch (headerName) {
                case "Transaction date" -> actualValues.add(t.getDate() == null ? null : FORMATTER.format(t.getDate()));
                case "Transaction Type" -> actualValues.add(t.getTransactionType() == null ? null : t.getTransactionType().getValue());
                case "Amount" ->
                    actualValues.add(t.getAmount() == null ? null : new Utils.DoubleFormatter(t.getAmount().doubleValue()).format());
                case "Balance" -> actualValues.add(
                        t.getRunningBalance() == null ? null : new Utils.DoubleFormatter(t.getRunningBalance().doubleValue()).format());
                case "Reverted" -> actualValues.add(String.valueOf(t.getReversed()));

                default -> throw new IllegalStateException(String.format("Header name %s cannot be found", headerName));
            }
        }
        return actualValues;
    }

    private PostSavingsAccountsResponse createSavingsAccount(String submittedOnDate) {
        PostClientsResponse clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        long clientId = clientResponse.getClientId();
        PostSavingsProductsResponse savingsProductResponse = testContext().get(TestContextKey.DEFAULT_SAVINGS_PRODUCT_CREATE_RESPONSE_EUR);
        long productId = savingsProductResponse.getResourceId();

        PostSavingsAccountsRequest request = SavingsAccountRequestFactory.defaultEURSavingsAccountRequest().clientId(clientId)
                .productId(productId).submittedOnDate(submittedOnDate);
        return ok(() -> corevanceClient.savingsAccount().submitSavingsApplication(request));
    }

    private Long getNamedSavingsAccountId(String currency, String accountAlias) {
        PostSavingsAccountsResponse savingsAccountResponse = testContext().get(namedSavingsAccountCreateKey(currency, accountAlias));
        return savingsAccountResponse.getSavingsId();
    }

    private String namedSavingsAccountCreateKey(String currency, String accountAlias) {
        return currency + "_SAVINGS_ACCOUNT_CREATE_RESPONSE_" + accountAlias;
    }

    private String namedSavingsAccountApproveKey(String currency, String accountAlias) {
        return currency + "_SAVINGS_ACCOUNT_APPROVE_RESPONSE_" + accountAlias;
    }

    private String namedSavingsAccountActivateKey(String currency, String accountAlias) {
        return currency + "_SAVINGS_ACCOUNT_ACTIVATED_RESPONSE_" + accountAlias;
    }

    private String namedSavingsAccountDepositKey(String currency, String accountAlias) {
        return currency + "_SAVINGS_ACCOUNT_DEPOSIT_RESPONSE_" + accountAlias;
    }
}
