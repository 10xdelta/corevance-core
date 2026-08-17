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
package com.corevance.integrationtests.client.feign.helpers;

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.util.Map;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.DeleteSavingsAccountsAccountIdResponse;
import com.corevance.client.models.PostSavingsAccountsAccountIdRequest;
import com.corevance.client.models.PostSavingsAccountsAccountIdResponse;
import com.corevance.client.models.PostSavingsAccountsRequest;
import com.corevance.client.models.PostSavingsAccountsResponse;
import com.corevance.client.models.SavingsAccountData;
import com.corevance.integrationtests.client.feign.modules.SavingsRequestBuilders;

public class FeignSavingsHelper {

    private final CorevanceFeignClient corevanceClient;

    public FeignSavingsHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public PostSavingsAccountsResponse submitApplication(PostSavingsAccountsRequest request) {
        return ok(() -> corevanceClient.savingsAccount().submitSavingsApplication(request));
    }

    public PostSavingsAccountsResponse submitApplication(Long clientId, Long productId, String submittedOnDate) {
        return submitApplication(SavingsRequestBuilders.submitSavingsApplication(clientId, productId, submittedOnDate));
    }

    public PostSavingsAccountsAccountIdResponse approveSavings(Long savingsId, String approvedOnDate) {
        PostSavingsAccountsAccountIdRequest request = SavingsRequestBuilders.approveSavings(approvedOnDate);
        return ok(() -> corevanceClient.savingsAccount().handleCommandsSavingsAccount(savingsId, request, "approve"));
    }

    public PostSavingsAccountsAccountIdResponse undoApproval(Long savingsId) {
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        return ok(() -> corevanceClient.savingsAccount().handleCommandsSavingsAccount(savingsId, request, "undoApproval"));
    }

    public PostSavingsAccountsAccountIdResponse activateSavings(Long savingsId, String activatedOnDate) {
        PostSavingsAccountsAccountIdRequest request = SavingsRequestBuilders.activateSavings(activatedOnDate);
        return ok(() -> corevanceClient.savingsAccount().handleCommandsSavingsAccount(savingsId, request, "activate"));
    }

    public PostSavingsAccountsAccountIdResponse rejectSavings(Long savingsId, String rejectedOnDate) {
        PostSavingsAccountsAccountIdRequest request = SavingsRequestBuilders.rejectSavings(rejectedOnDate);
        return ok(() -> corevanceClient.savingsAccount().handleCommandsSavingsAccount(savingsId, request, "reject"));
    }

    public PostSavingsAccountsAccountIdResponse closeSavings(Long savingsId, String closedOnDate, boolean withdrawBalance) {
        PostSavingsAccountsAccountIdRequest request = SavingsRequestBuilders.closeSavings(closedOnDate, withdrawBalance);
        return ok(() -> corevanceClient.savingsAccount().handleCommandsSavingsAccount(savingsId, request, "close"));
    }

    public SavingsAccountData getSavingsDetails(Long savingsId) {
        return ok(() -> corevanceClient.savingsAccount().retrieveSavingsAccount(savingsId, Map.of("associations", "all")));
    }

    public SavingsAccountData getSavingsDetails(Long savingsId, String associations) {
        return ok(() -> corevanceClient.savingsAccount().retrieveSavingsAccount(savingsId, Map.of("associations", associations)));
    }

    public DeleteSavingsAccountsAccountIdResponse deleteSavingsApplication(Long savingsId) {
        return ok(() -> corevanceClient.savingsAccount().deleteSavingsAccount(savingsId));
    }

    public Long createApproveActivateSavings(Long clientId, Long productId, String date) {
        Long savingsId = submitApplication(clientId, productId, date).getSavingsId();
        approveSavings(savingsId, date);
        activateSavings(savingsId, date);
        return savingsId;
    }
}
