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

import java.time.format.DateTimeFormatter;
import com.corevance.client.models.PostSavingsAccountsAccountIdRequest;
import com.corevance.client.models.PostSavingsAccountsAccountIdResponse;
import com.corevance.client.models.PostSavingsAccountsRequest;
import com.corevance.client.models.PostSavingsAccountsResponse;
import com.corevance.integrationtests.client.IntegrationTest;
import com.corevance.integrationtests.common.Utils;
import com.corevance.integrationtests.common.savings.SavingsTestLifecycleExtension;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

/**
 * Integration Test for /savingsaccounts API.
 *
 * @author Danish Jamal
 *
 */
@ExtendWith({ SavingsTestLifecycleExtension.class })
public class SavingsAccountsTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccountsTest.class);
    private final String dateFormat = "dd MMMM yyyy";
    private final String locale = "en";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
    private final String formattedDate = dateFormatter.format(Utils.getLocalDateOfTenant());
    private int savingId = 1;

    @Test
    @Order(1)
    void submitSavingsAccountsApplication() {
        LOG.info("------------------------------ CREATING NEW SAVINGS ACCOUNT APPLICATION ---------------------------------------");
        PostSavingsAccountsRequest request = new PostSavingsAccountsRequest();
        request.setClientId(1L);
        request.setProductId(1L);
        request.setLocale(locale);
        request.setDateFormat(dateFormat);
        request.submittedOnDate(formattedDate);

        Response<PostSavingsAccountsResponse> response = okR(corevanceClient().savingsAccounts.submitSavingsApplication(request));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
        savingId = Math.toIntExact(response.body().getSavingsId());
    }

    @Test
    @Order(2)
    void approveSavingsAccount() {
        LOG.info("------------------------------ APPROVING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setApprovedOnDate(formattedDate);
        Response<PostSavingsAccountsAccountIdResponse> response = okR(
                corevanceClient().savingsAccounts.handleCommandsSavingsAccount((long) savingId, request, "approve"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    @Test
    @Order(3)
    void activateSavingsAccount() {
        LOG.info("------------------------------ ACTIVATING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<PostSavingsAccountsAccountIdResponse> response = okR(
                corevanceClient().savingsAccounts.handleCommandsSavingsAccount((long) savingId, request, "activate"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

}
