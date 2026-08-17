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

import static com.corevance.client.feign.util.FeignCalls.fail;
import static com.corevance.client.feign.util.FeignCalls.ok;

import java.util.Collections;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.feign.util.CallFailedRuntimeException;
import com.corevance.client.models.ClientTextSearch;
import com.corevance.client.models.DeleteClientsClientIdResponse;
import com.corevance.client.models.GetClientsClientIdAccountsResponse;
import com.corevance.client.models.GetClientsClientIdResponse;
import com.corevance.client.models.PageClientSearchData;
import com.corevance.client.models.PagedRequestClientTextSearch;
import com.corevance.client.models.PostClientsClientIdRequest;
import com.corevance.client.models.PostClientsClientIdResponse;
import com.corevance.client.models.PostClientsRequest;
import com.corevance.client.models.PostClientsResponse;
import com.corevance.client.models.PutClientsClientIdRequest;
import com.corevance.client.models.PutClientsClientIdResponse;
import com.corevance.integrationtests.client.feign.modules.ClientRequestBuilders;
import com.corevance.integrationtests.client.feign.modules.LoanTestData;
import com.corevance.integrationtests.common.Utils;

public class FeignClientHelper {

    private static final String ACTIVATE_COMMAND = "activate";
    private static final String CLOSE_COMMAND = "close";
    private static final String REJECT_COMMAND = "reject";
    private static final String REACTIVATE_COMMAND = "reactivate";
    private static final String WITHDRAW_COMMAND = "withdraw";
    private static final String UNDO_REJECTION_COMMAND = "undoRejection";
    private static final String UNDO_WITHDRAWAL_COMMAND = "undoWithdrawal";

    private final CorevanceFeignClient corevanceClient;

    public FeignClientHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public Long createClient() {
        return createClient(com.corevance.integrationtests.common.ClientHelper.DEFAULT_DATE);
    }

    public Long createClient(String activationDate) {
        String externalId = Utils.randomStringGenerator("EXT_", 7);

        PostClientsRequest request = new PostClientsRequest()//
                .officeId(1L)//
                .legalFormId(1L)//
                .firstname(Utils.randomFirstNameGenerator())//
                .lastname(Utils.randomLastNameGenerator())//
                .externalId(externalId)//
                .active(true)//
                .activationDate(activationDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE);

        return createClient(request).getClientId();
    }

    public PostClientsResponse createClient(PostClientsRequest request) {
        return ok(() -> corevanceClient.clients().createClient(request));
    }

    public PostClientsResponse createClientPending() {
        return createClientPending(Utils.dateFormatter.format(Utils.getLocalDateOfTenant()));
    }

    public PostClientsResponse createClientPending(String submittedOnDate) {
        return createClientPending(ClientRequestBuilders.createPendingClient(submittedOnDate));
    }

    public PostClientsResponse createClientPending(PostClientsRequest request) {
        return ok(() -> corevanceClient.clients().createClient(request));
    }

    public GetClientsClientIdResponse getClient(Long clientId) {
        return ok(() -> corevanceClient.clients().retrieveOneClient(clientId, Collections.emptyMap()));
    }

    public CallFailedRuntimeException getClientExpectingError(Long clientId) {
        return fail(() -> corevanceClient.clients().retrieveOneClient(clientId, Collections.emptyMap()));
    }

    public GetClientsClientIdAccountsResponse getClientAccounts(Long clientId) {
        return ok(() -> corevanceClient.clients().retrieveAllClientAccounts(clientId));
    }

    public PageClientSearchData searchClients(String text) {
        ClientTextSearch clientTextSearch = new ClientTextSearch();
        clientTextSearch.setText(text);
        PagedRequestClientTextSearch request = new PagedRequestClientTextSearch();
        request.setRequest(clientTextSearch);
        return ok(() -> corevanceClient.clientSearchV2().searchClientsByText(request));
    }

    public PostClientsClientIdResponse activateClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> corevanceClient.clients().handleCommandClient(clientId, request, ACTIVATE_COMMAND));
    }

    public PostClientsClientIdResponse closeClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> corevanceClient.clients().handleCommandClient(clientId, request, CLOSE_COMMAND));
    }

    public PostClientsClientIdResponse rejectClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> corevanceClient.clients().handleCommandClient(clientId, request, REJECT_COMMAND));
    }

    public PostClientsClientIdResponse reactivateClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> corevanceClient.clients().handleCommandClient(clientId, request, REACTIVATE_COMMAND));
    }

    public PostClientsClientIdResponse withdrawClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> corevanceClient.clients().handleCommandClient(clientId, request, WITHDRAW_COMMAND));
    }

    public PostClientsClientIdResponse undoRejectClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> corevanceClient.clients().handleCommandClient(clientId, request, UNDO_REJECTION_COMMAND));
    }

    public PostClientsClientIdResponse undoWithdrawnClient(Long clientId, PostClientsClientIdRequest request) {
        return ok(() -> corevanceClient.clients().handleCommandClient(clientId, request, UNDO_WITHDRAWAL_COMMAND));
    }

    public PutClientsClientIdResponse updateClient(Long clientId, PutClientsClientIdRequest request) {
        return ok(() -> corevanceClient.clients().updateClient(clientId, request));
    }

    public DeleteClientsClientIdResponse deleteClient(Long clientId) {
        return ok(() -> corevanceClient.clients().deleteClient(clientId));
    }
}
