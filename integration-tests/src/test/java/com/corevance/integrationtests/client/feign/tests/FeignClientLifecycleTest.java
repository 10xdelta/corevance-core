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
package com.corevance.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.feign.util.CallFailedRuntimeException;
import com.corevance.client.models.GetClientsClientIdAccountsResponse;
import com.corevance.client.models.GetClientsClientIdResponse;
import com.corevance.client.models.GetCodesResponse;
import com.corevance.client.models.PageClientSearchData;
import com.corevance.client.models.PostClientsClientIdResponse;
import com.corevance.client.models.PostClientsResponse;
import com.corevance.client.models.PostCodeValueDataResponse;
import com.corevance.client.models.PostCodeValuesDataRequest;
import com.corevance.client.models.PutClientsClientIdResponse;
import com.corevance.integrationtests.client.FeignIntegrationTest;
import com.corevance.integrationtests.client.feign.helpers.FeignClientHelper;
import com.corevance.integrationtests.client.feign.modules.ClientRequestBuilders;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;
import com.corevance.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeignClientLifecycleTest extends FeignIntegrationTest {

    private static final String STATUS_PENDING = "clientStatusType.pending";
    private static final String STATUS_ACTIVE = "clientStatusType.active";
    private static final String STATUS_CLOSED = "clientStatusType.closed";
    private static final String STATUS_REJECTED = "clientStatusType.rejected";
    private static final String STATUS_WITHDRAWN = "clientStatusType.withdraw";

    private static FeignClientHelper clientHelper;
    private static CorevanceFeignClient corevanceClient;

    @BeforeAll
    public static void setup() {
        corevanceClient = CorevanceFeignClientHelper.getCorevanceFeignClient();
        clientHelper = new FeignClientHelper(corevanceClient);
    }

    private Long resolveOrCreateCodeValue(String codeName) {
        GetCodesResponse code = ok(() -> corevanceClient.codes().retrieveOneCodeByName(codeName));
        assertNotNull(code.getId());

        PostCodeValuesDataRequest createRequest = new PostCodeValuesDataRequest()//
                .name(Utils.randomStringGenerator(codeName + "_", 5))//
                .isActive(true);
        PostCodeValueDataResponse created = ok(() -> corevanceClient.codeValues().createCodeValue(code.getId(), createRequest));
        return created.getSubResourceId();
    }

    private void assertClientStatus(Long clientId, String expectedStatusCode) {
        GetClientsClientIdResponse details = clientHelper.getClient(clientId);
        assertNotNull(details.getStatus());
        assertEquals(expectedStatusCode, details.getStatus().getCode());
    }

    @Test
    @Order(1)
    void testCreatePendingAndActivateClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        PostClientsResponse pending = clientHelper.createClientPending(today);
        assertNotNull(pending.getClientId());
        assertClientStatus(pending.getClientId(), STATUS_PENDING);

        PostClientsClientIdResponse activateResp = clientHelper.activateClient(pending.getClientId(),
                ClientRequestBuilders.activateClient(today));
        assertNotNull(activateResp.getClientId());
        assertClientStatus(pending.getClientId(), STATUS_ACTIVE);
    }

    @Test
    @Order(2)
    void testCreatePendingAndRejectClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        PostClientsResponse pending = clientHelper.createClientPending(today);
        assertNotNull(pending.getClientId());

        Long rejectionReasonId = resolveOrCreateCodeValue("ClientRejectReason");

        PostClientsClientIdResponse rejectResp = clientHelper.rejectClient(pending.getClientId(),
                ClientRequestBuilders.rejectClient(rejectionReasonId, today));
        assertNotNull(rejectResp.getClientId());
        assertClientStatus(pending.getClientId(), STATUS_REJECTED);
    }

    @Test
    @Order(3)
    void testRejectAndUndoRejectClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        PostClientsResponse pending = clientHelper.createClientPending(today);
        assertNotNull(pending.getClientId());

        Long rejectionReasonId = resolveOrCreateCodeValue("ClientRejectReason");
        clientHelper.rejectClient(pending.getClientId(), ClientRequestBuilders.rejectClient(rejectionReasonId, today));
        assertClientStatus(pending.getClientId(), STATUS_REJECTED);

        PostClientsClientIdResponse undoResp = clientHelper.undoRejectClient(pending.getClientId(),
                ClientRequestBuilders.undoRejectClient(today));
        assertNotNull(undoResp.getClientId());
        assertClientStatus(pending.getClientId(), STATUS_PENDING);
    }

    @Test
    @Order(4)
    void testWithdrawClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        PostClientsResponse pending = clientHelper.createClientPending(today);
        assertNotNull(pending.getClientId());

        Long withdrawalReasonId = resolveOrCreateCodeValue("ClientWithdrawReason");

        PostClientsClientIdResponse withdrawResp = clientHelper.withdrawClient(pending.getClientId(),
                ClientRequestBuilders.withdrawClient(withdrawalReasonId, today));
        assertNotNull(withdrawResp.getClientId());
        assertClientStatus(pending.getClientId(), STATUS_WITHDRAWN);
    }

    @Test
    @Order(5)
    void testWithdrawAndUndoWithdrawClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        PostClientsResponse pending = clientHelper.createClientPending(today);
        assertNotNull(pending.getClientId());

        Long withdrawalReasonId = resolveOrCreateCodeValue("ClientWithdrawReason");
        clientHelper.withdrawClient(pending.getClientId(), ClientRequestBuilders.withdrawClient(withdrawalReasonId, today));
        assertClientStatus(pending.getClientId(), STATUS_WITHDRAWN);

        PostClientsClientIdResponse undoResp = clientHelper.undoWithdrawnClient(pending.getClientId(),
                ClientRequestBuilders.undoWithdrawnClient(today));
        assertNotNull(undoResp.getClientId());
        assertClientStatus(pending.getClientId(), STATUS_PENDING);
    }

    @Test
    @Order(6)
    void testCloseAndReactivateClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        Long clientId = clientHelper.createClient(today);

        Long closureReasonId = resolveOrCreateCodeValue("ClientClosureReason");

        PostClientsClientIdResponse closeResp = clientHelper.closeClient(clientId,
                ClientRequestBuilders.closeClient(closureReasonId, today));
        assertNotNull(closeResp.getClientId());
        assertClientStatus(clientId, STATUS_CLOSED);

        PostClientsClientIdResponse reactivateResp = clientHelper.reactivateClient(clientId, ClientRequestBuilders.reactivateClient(today));
        assertNotNull(reactivateResp.getClientId());
        assertClientStatus(clientId, STATUS_PENDING);
    }

    @Test
    @Order(7)
    void testUpdateClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        Long clientId = clientHelper.createClient(today);

        String newFirstName = Utils.randomFirstNameGenerator();
        String newLastName = Utils.randomLastNameGenerator();
        PutClientsClientIdResponse updateResp = clientHelper.updateClient(clientId,
                ClientRequestBuilders.updateClientName(newFirstName, newLastName));
        assertNotNull(updateResp.getClientId());

        GetClientsClientIdResponse details = clientHelper.getClient(clientId);
        assertEquals(newFirstName, details.getFirstname());
        assertEquals(newLastName, details.getLastname());
    }

    @Test
    @Order(8)
    void testDeletePendingClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        PostClientsResponse pending = clientHelper.createClientPending(today);
        assertNotNull(pending.getClientId());

        clientHelper.deleteClient(pending.getClientId());

        CallFailedRuntimeException exception = clientHelper.getClientExpectingError(pending.getClientId());
        assertEquals(404, exception.getStatus());
    }

    @Test
    @Order(9)
    void testSearchClient() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        String uniqueLastName = Utils.randomStringGenerator("SearchTest_", 8);
        Long clientId = clientHelper.createClient(today);

        GetClientsClientIdResponse client = clientHelper.getClient(clientId);
        String searchName = client.getLastname();

        PageClientSearchData results = clientHelper.searchClients(searchName);
        assertNotNull(results);
        assertNotNull(results.getContent());
        assertFalse(results.getContent().isEmpty());
        assertTrue(results.getContent().stream().anyMatch(r -> r.getId().equals(clientId)));
    }

    @Test
    @Order(10)
    void testGetClientAccounts() {
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

        Long clientId = clientHelper.createClient(today);

        GetClientsClientIdAccountsResponse accounts = clientHelper.getClientAccounts(clientId);
        assertNotNull(accounts);
    }
}
