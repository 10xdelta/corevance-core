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
package com.corevance.integrationtests.common;

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.time.LocalDate;
import com.corevance.client.models.GetOfficesResponse;
import com.corevance.client.models.PostOfficesRequest;
import com.corevance.client.models.PostOfficesResponse;
import com.corevance.client.models.PutOfficesOfficeIdRequest;
import com.corevance.client.models.PutOfficesOfficeIdResponse;

public class OfficeHelper {

    public static final long HEAD_OFFICE_ID = 1L; // The ID is hardcoded in the initial Liquibase migration script

    public GetOfficesResponse retrieveOffice(Long officeId) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().offices().retrieveOneOffice(officeId));
    }

    public static GetOfficesResponse getHeadOffice() {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().offices().retrieveOneOffice(HEAD_OFFICE_ID));
    }

    public PostOfficesResponse createOffice(final LocalDate openingDate) {
        PostOfficesRequest request = new PostOfficesRequest()//
                .parentId(HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("O_", 9))//
                .openingDate(openingDate)//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().offices().createOffice(request));
    }

    public PostOfficesResponse createOffice(final String externalId, final LocalDate openingDate) {
        PostOfficesRequest request = new PostOfficesRequest()//
                .parentId(HEAD_OFFICE_ID)//
                .name(Utils.uniqueRandomStringGenerator("O_", 9))//
                .externalId(externalId)//
                .openingDate(openingDate)//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().offices().createOffice(request));
    }

    public PutOfficesOfficeIdResponse updateOffice(Long officeId, String name, String openingDate) {
        PutOfficesOfficeIdRequest request = new PutOfficesOfficeIdRequest()//
                .name(name)//
                .openingDate(openingDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().offices().updateOffice(officeId, request));
    }

    public GetOfficesResponse retrieveOfficeByExternalId(String externalId) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().offices().retrieveOneOfficeByExternalId(externalId));
    }

    public PutOfficesOfficeIdResponse updateOfficeByExternalId(String externalId, String name, String openingDate) {
        PutOfficesOfficeIdRequest request = new PutOfficesOfficeIdRequest()//
                .name(name)//
                .openingDate(openingDate)//
                .dateFormat("dd MMMM yyyy")//
                .locale("en");
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().offices().updateOfficeByExternalId(externalId, request));
    }
}
