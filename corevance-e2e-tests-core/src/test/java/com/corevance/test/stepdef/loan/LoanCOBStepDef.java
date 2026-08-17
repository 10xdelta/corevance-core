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

import static com.corevance.client.feign.util.FeignCalls.executeVoid;
import static com.corevance.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.LoanAccountLock;
import com.corevance.client.models.LoanAccountLockResponseDTO;
import com.corevance.client.models.LockRequest;
import com.corevance.client.models.OldestCOBProcessedLoanDTO;
import com.corevance.client.models.PostLoansResponse;
import com.corevance.test.helper.ErrorMessageHelper;
import com.corevance.test.stepdef.AbstractStepDef;
import com.corevance.test.support.TestContextKey;
import org.junit.jupiter.api.Assertions;

@Slf4j
@RequiredArgsConstructor
public class LoanCOBStepDef extends AbstractStepDef {

    private final CorevanceFeignClient corevanceClient;

    @Then("The cobProcessedDate of the oldest loan processed by COB is more than 1 day earlier than cobBusinessDate")
    public void checkOldestCOBProcessed() {
        OldestCOBProcessedLoanDTO response = ok(() -> corevanceClient.loanCobCatchUp().getOldestCOBProcessedLoan());

        LocalDate cobDate = response.getCobBusinessDate();
        Assertions.assertNotNull(cobDate);
        LocalDate cobDateMinusOne = cobDate.minusDays(1);
        LocalDate cobProcessedDate = response.getCobProcessedDate();
        log.debug("cobDateMinusOne: {}", cobDateMinusOne);
        log.debug("cobProcessedDate: {}", cobProcessedDate);

        boolean result = cobDateMinusOne.isAfter(cobProcessedDate);
        assertThat(result).as(ErrorMessageHelper.wrongLastCOBProcessedLoanDate(cobProcessedDate, cobDateMinusOne)).isTrue();
    }

    @Then("There are no locked loan accounts")
    public void listOfLockedLoansEmpty() {
        LoanAccountLockResponseDTO response = ok(
                () -> corevanceClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        int size = response.getContent().size();
        assertThat(size).as(ErrorMessageHelper.listOfLockedLoansNotEmpty(response)).isEqualTo(0);
        log.debug("Size of List of the locked loans: {}", size);
    }

    @Then("The loan account is not locked")
    public void loanIsNotInListOfLockedLoans() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long targetLoanId = loanResponse.getLoanId();

        LoanAccountLockResponseDTO response = ok(
                () -> corevanceClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        Assertions.assertNotNull(targetLoanId);
        List<LoanAccountLock> content = response.getContent();
        boolean contains = content.stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);//

        assertThat(contains).as(ErrorMessageHelper.listOfLockedLoansContainsLoan(targetLoanId, response)).isFalse();
    }

    @Then("The loan account is locked by chunk processing")
    public void loanIsLockedByChunkProcessing() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long targetLoanId = loanResponse.getLoanId();

        final LoanAccountLockResponseDTO response = ok(
                () -> corevanceClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        Assertions.assertNotNull(targetLoanId);
        final boolean stillLocked = response.getContent().stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);//

        assertThat(stillLocked).as(ErrorMessageHelper.expectedLoanToRemainLocked(targetLoanId, response)).isTrue();
    }

    @When("Admin places a lock on loan account with an error message")
    public void placeLockOnLoanAccountWithErrorMessage() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        executeVoid(() -> corevanceClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().error("ERROR")));
    }

    @When("Admin places a lock on loan account WITHOUT an error message")
    public void placeLockOnLoanAccountNoErrorMessage() {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        Long loanId = loanResponse.getLoanId();

        executeVoid(() -> corevanceClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING", new LockRequest()));
    }

    @When("Admin places an inline COB lock on loan account WITHOUT an error message")
    public void placeInlineLockOnLoanAccountNoErrorMessage() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> corevanceClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_INLINE_COB_PROCESSING", new LockRequest()));
    }

    @When("Admin places an inline COB lock on loan account with an error message")
    public void placeInlineLockOnLoanAccountWithErrorMessage() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> corevanceClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_INLINE_COB_PROCESSING",
                new LockRequest().error("ERROR")));
    }

    @When("Admin places a lock on loan account WITHOUT an error message and null cob business date")
    public void placeLockOnLoanAccountWithNullCobBusinessDate() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> corevanceClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().nullCobBusinessDate(true)));
    }

    @When("Admin places a lock on loan account WITHOUT an error message and cob business date {string}")
    public void placeLockOnLoanAccountWithExplicitCobBusinessDate(final String cobBusinessDate) {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        final Long loanId = loanResponse.getLoanId();
        final LocalDate parsed = LocalDate.parse(cobBusinessDate, DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        executeVoid(() -> corevanceClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().cobBusinessDate(parsed)));
    }

    @When("Admin places a lock on second loan account WITHOUT an error message")
    public void placeLockOnSecondLoanAccountNoErrorMessage() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> corevanceClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING", new LockRequest()));
    }

    @When("Admin places a lock on second loan account with an error message")
    public void placeLockOnSecondLoanAccountWithErrorMessage() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        final Long loanId = loanResponse.getLoanId();

        executeVoid(() -> corevanceClient.loanAccountLock().placeLockOnLoanAccount(loanId, "LOAN_COB_CHUNK_PROCESSING",
                new LockRequest().error("ERROR")));
    }

    @Then("The second loan account is not locked")
    public void secondLoanIsNotInListOfLockedLoans() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        final Long targetLoanId = loanResponse.getLoanId();

        final LoanAccountLockResponseDTO response = ok(
                () -> corevanceClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        Assertions.assertNotNull(targetLoanId);
        final boolean contains = response.getContent().stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);

        assertThat(contains).as(ErrorMessageHelper.listOfLockedLoansContainsLoan(targetLoanId, response)).isFalse();
    }

    @Then("The second loan account is locked by chunk processing")
    public void secondLoanIsLockedByChunkProcessing() {
        final PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_SECOND_LOAN_RESPONSE);
        final Long targetLoanId = loanResponse.getLoanId();

        final LoanAccountLockResponseDTO response = ok(
                () -> corevanceClient.loanAccountLock().retrieveLockedAccounts(Map.of("page", 0, "size", 1000)));

        Assertions.assertNotNull(response.getContent());
        Assertions.assertNotNull(targetLoanId);
        final boolean stillLocked = response.getContent().stream()//
                .map(LoanAccountLock::getLoanId)//
                .anyMatch(targetLoanId::equals);

        assertThat(stillLocked).as(ErrorMessageHelper.expectedLoanToRemainLocked(targetLoanId, response)).isTrue();
    }
}
