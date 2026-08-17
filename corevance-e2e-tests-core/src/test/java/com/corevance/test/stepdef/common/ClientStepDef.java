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
package com.corevance.test.stepdef.common;

import static com.corevance.client.feign.util.FeignCalls.ok;
import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.ClientAddressRequest;
import com.corevance.client.models.PostClientsRequest;
import com.corevance.client.models.PostClientsResponse;
import com.corevance.client.models.PostOfficesResponse;
import com.corevance.test.factory.ClientRequestFactory;
import com.corevance.test.messaging.event.EventCheckHelper;
import com.corevance.test.stepdef.AbstractStepDef;
import com.corevance.test.support.TestContextKey;

@RequiredArgsConstructor
public class ClientStepDef extends AbstractStepDef {

    private final CorevanceFeignClient corevanceClient;
    private final ClientRequestFactory clientRequestFactory;
    private final EventCheckHelper eventCheckHelper;

    @When("Admin creates a client with random data")
    public void createClientRandomFirstNameLastName() {
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();

        PostClientsResponse response = ok(() -> corevanceClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);

        eventCheckHelper.clientEventCheck(response);
    }

    @When("Admin creates a client with random data in the last created office")
    public void createClientInLastCreatedOffice() {
        final PostOfficesResponse officeResponse = testContext().get(TestContextKey.OFFICE_CREATE_RESPONSE);
        assertThat(officeResponse).as("No office was created. Use 'Admin creates a new office' step first.").isNotNull();
        final PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest()//
                .officeId(officeResponse.getOfficeId());

        final PostClientsResponse response = ok(() -> corevanceClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);

        eventCheckHelper.clientEventCheck(response);
    }

    @When("Admin creates a second client with random data")
    public void createSecondClientRandomFirstNameLastName() {
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();

        PostClientsResponse response = ok(() -> corevanceClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_SECOND_CLIENT_RESPONSE, response);

        eventCheckHelper.clientEventCheck(response);
    }

    @When("Admin creates a client with Firstname {string} and Lastname {string}")
    public void createClient(String firstName, String lastName) {
        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest().firstname(firstName).lastname(lastName);

        PostClientsResponse response = ok(() -> corevanceClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);
    }

    @When("Admin creates a client with Firstname {string} and Lastname {string} with address")
    public void createClientWithAddress(String firstName, String lastName) {
        Long addressTypeId = 15L;
        Long countryId = 17L;
        Long stateId = 18L;
        String city = "Budapest";
        boolean addressIsActive = true;
        String postalCode = "1000";

        ClientAddressRequest addressRequest = new ClientAddressRequest().postalCode(postalCode).city(city).countryId(countryId)
                .stateProvinceId(stateId).addressTypeId(addressTypeId).isActive(addressIsActive);

        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest().firstname(firstName).lastname(lastName)
                .address(Collections.singletonList(addressRequest));

        PostClientsResponse response = ok(() -> corevanceClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);

    }

    @When("Admin creates a client with Firstname {string} and Lastname {string} with {string} activation date")
    public void createClientWithSpecifiedDates(String firstName, String lastName, String activationDate) {

        PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest().firstname(firstName).lastname(lastName)
                .activationDate(activationDate);

        PostClientsResponse response = ok(() -> corevanceClient.clients().createClient(clientsRequest));
        testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, response);
    }

    @Then("Client is created successfully")
    public void checkClientCreatedSuccessfully() {
        PostClientsResponse response = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);

        assertThat(response.getClientId()).isNotNull();

        eventCheckHelper.clientEventCheck(response);
    }
}
