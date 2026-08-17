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

import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.GetCodesResponse;
import com.corevance.client.models.PostCodeValueDataResponse;
import com.corevance.client.models.PostCodeValuesDataRequest;

public class FeignCodeHelper {

    private static final String CHARGE_OFF_REASONS_CODE_NAME = "ChargeOffReasons";

    private final CorevanceFeignClient corevanceClient;

    public FeignCodeHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public Long createChargeOffCodeValue(String value, Integer position) {
        GetCodesResponse code = ok(() -> corevanceClient.codes().retrieveOneCodeByName(CHARGE_OFF_REASONS_CODE_NAME));
        PostCodeValueDataResponse response = ok(() -> corevanceClient.codeValues().createCodeValue(code.getId(),
                new PostCodeValuesDataRequest().name(value).position(position).description(value).isActive(true)));
        return response.getSubResourceId();
    }

    public GetCodesResponse retrieveCodeByName(String codeName) {
        return ok(() -> corevanceClient.codes().retrieveOneCodeByName(codeName));
    }

    public PostCodeValueDataResponse createCodeValue(Long codeId, PostCodeValuesDataRequest request) {
        return ok(() -> corevanceClient.codeValues().createCodeValue(codeId, request));
    }
}
