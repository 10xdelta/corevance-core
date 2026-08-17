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

import java.time.LocalDate;
import java.util.List;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.GetOfficesResponse;
import com.corevance.client.models.PostOfficesRequest;
import com.corevance.client.models.PostOfficesResponse;
import com.corevance.client.models.PutOfficesOfficeIdRequest;
import com.corevance.client.models.PutOfficesOfficeIdResponse;
import com.corevance.integrationtests.common.Utils;

public class FeignOfficeHelper {

    public static final long HEAD_OFFICE_ID = 1L;

    private final CorevanceFeignClient corevanceClient;

    public FeignOfficeHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public PostOfficesResponse createOffice(LocalDate openingDate) {
        PostOfficesRequest request = new PostOfficesRequest()//
                .parentId(HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("O_", 9))//
                .openingDate(openingDate)//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");
        return ok(() -> corevanceClient.offices().createOffice(request));
    }

    public PostOfficesResponse createOffice(String externalId, LocalDate openingDate) {
        PostOfficesRequest request = new PostOfficesRequest()//
                .parentId(HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("O_", 9))//
                .externalId(externalId)//
                .openingDate(openingDate)//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");
        return ok(() -> corevanceClient.offices().createOffice(request));
    }

    public GetOfficesResponse retrieveOffice(Long officeId) {
        return ok(() -> corevanceClient.offices().retrieveOneOffice(officeId));
    }

    public GetOfficesResponse retrieveOfficeByExternalId(String externalId) {
        return ok(() -> corevanceClient.offices().retrieveOneOfficeByExternalId(externalId));
    }

    public GetOfficesResponse getHeadOffice() {
        return ok(() -> corevanceClient.offices().retrieveOneOffice(HEAD_OFFICE_ID));
    }

    public PutOfficesOfficeIdResponse updateOffice(Long officeId, String name, String openingDate) {
        PutOfficesOfficeIdRequest request = new PutOfficesOfficeIdRequest()//
                .name(name)//
                .openingDate(openingDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        return ok(() -> corevanceClient.offices().updateOffice(officeId, request));
    }

    public PutOfficesOfficeIdResponse updateOfficeByExternalId(String externalId, String name, String openingDate) {
        PutOfficesOfficeIdRequest request = new PutOfficesOfficeIdRequest()//
                .name(name)//
                .openingDate(openingDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        return ok(() -> corevanceClient.offices().updateOfficeByExternalId(externalId, request));
    }

    public List<GetOfficesResponse> retrieveAllOffices() {
        return ok(() -> corevanceClient.offices().retrieveAllOffices(false, null, null));
    }
}
