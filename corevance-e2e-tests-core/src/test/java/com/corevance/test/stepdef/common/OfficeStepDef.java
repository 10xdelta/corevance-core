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

import io.cucumber.java.en.When;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.PostOfficesRequest;
import com.corevance.client.models.PostOfficesResponse;
import com.corevance.test.helper.Utils;
import com.corevance.test.stepdef.AbstractStepDef;
import com.corevance.test.support.TestContextKey;

@RequiredArgsConstructor
public class OfficeStepDef extends AbstractStepDef {

    private final CorevanceFeignClient corevanceClient;

    @When("Admin creates a new office")
    public void createNewOffice() {
        final PostOfficesRequest request = new PostOfficesRequest()//
                .name(Utils.randomStringGenerator("Office_", 5))//
                .parentId(1L)//
                .openingDate(LocalDate.of(2000, 1, 1))//
                .dateFormat("yyyy-MM-dd")//
                .locale("en");//

        final PostOfficesResponse response = ok(() -> corevanceClient.offices().createOffice(request));
        testContext().set(TestContextKey.OFFICE_CREATE_RESPONSE, response);
    }
}
