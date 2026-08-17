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
package com.corevance.integrationtests.client.feign;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.DeleteSavingsAccountsAccountIdResponse;
import com.corevance.client.models.GetSavingsProductsProductIdResponse;
import com.corevance.client.models.PostSavingsAccountTransactionsResponse;
import com.corevance.client.models.PostSavingsAccountsAccountIdResponse;
import com.corevance.client.models.PostSavingsAccountsResponse;
import com.corevance.client.models.PostSavingsProductsRequest;
import com.corevance.client.models.PostSavingsProductsResponse;
import com.corevance.client.models.SavingsAccountData;
import com.corevance.client.models.SavingsAccountStatusEnumData;
import com.corevance.integrationtests.client.FeignIntegrationTest;
import com.corevance.integrationtests.client.feign.helpers.FeignClientHelper;
import com.corevance.integrationtests.client.feign.helpers.FeignSavingsHelper;
import com.corevance.integrationtests.client.feign.helpers.FeignSavingsLifecycleExtension;
import com.corevance.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import com.corevance.integrationtests.client.feign.helpers.FeignSavingsTransactionHelper;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(FeignSavingsLifecycleExtension.class)
public abstract class FeignSavingsTestBase extends FeignIntegrationTest {

    protected static FeignSavingsHelper savingsHelper;
    protected static FeignSavingsTransactionHelper savingsTransactionHelper;
    protected static FeignSavingsProductHelper savingsProductHelper;
    protected static FeignClientHelper clientHelper;

    @BeforeAll
    public static void setupSavingsHelpers() {
        CorevanceFeignClient client = CorevanceFeignClientHelper.getCorevanceFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsTransactionHelper = new FeignSavingsTransactionHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        clientHelper = new FeignClientHelper(client);
    }

    protected Long createClient() {
        return clientHelper.createClient();
    }

    protected Long createClient(String activationDate) {
        return clientHelper.createClient(activationDate);
    }

    protected PostSavingsProductsResponse createDefaultSavingsProduct() {
        return savingsProductHelper.createDefaultSavingsProduct();
    }

    protected PostSavingsProductsResponse createSavingsProduct(PostSavingsProductsRequest request) {
        return savingsProductHelper.createSavingsProduct(request);
    }

    protected GetSavingsProductsProductIdResponse getSavingsProduct(Long productId) {
        return savingsProductHelper.getSavingsProduct(productId);
    }

    protected PostSavingsAccountsResponse submitSavingsApplication(Long clientId, Long productId, String date) {
        return savingsHelper.submitApplication(clientId, productId, date);
    }

    protected PostSavingsAccountsAccountIdResponse approveSavings(Long savingsId, String date) {
        return savingsHelper.approveSavings(savingsId, date);
    }

    protected PostSavingsAccountsAccountIdResponse activateSavings(Long savingsId, String date) {
        return savingsHelper.activateSavings(savingsId, date);
    }

    protected Long createApproveActivateSavings(Long clientId, Long productId, String date) {
        return savingsHelper.createApproveActivateSavings(clientId, productId, date);
    }

    protected PostSavingsAccountsAccountIdResponse closeSavings(Long savingsId, String date, boolean withdrawBalance) {
        return savingsHelper.closeSavings(savingsId, date, withdrawBalance);
    }

    protected DeleteSavingsAccountsAccountIdResponse deleteSavingsApplication(Long savingsId) {
        return savingsHelper.deleteSavingsApplication(savingsId);
    }

    protected SavingsAccountData getSavingsDetails(Long savingsId) {
        return savingsHelper.getSavingsDetails(savingsId);
    }

    protected PostSavingsAccountTransactionsResponse deposit(Long savingsId, String amount, String date) {
        return savingsTransactionHelper.deposit(savingsId, amount, date);
    }

    protected PostSavingsAccountTransactionsResponse withdraw(Long savingsId, String amount, String date) {
        return savingsTransactionHelper.withdraw(savingsId, amount, date);
    }

    protected void verifySavingsStatus(Long savingsId, Function<SavingsAccountStatusEnumData, Boolean> statusExtractor) {
        SavingsAccountData savings = getSavingsDetails(savingsId);
        assertNotNull(savings.getStatus(), "Savings status should not be null");
        assertTrue(statusExtractor.apply(savings.getStatus()), "Savings status check failed for account " + savingsId);
    }
}
