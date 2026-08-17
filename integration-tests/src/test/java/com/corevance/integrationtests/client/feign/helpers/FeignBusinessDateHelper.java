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

import java.util.Collections;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.BusinessDateResponse;
import com.corevance.client.models.BusinessDateUpdateRequest;
import com.corevance.integrationtests.client.feign.modules.LoanTestData;

public class FeignBusinessDateHelper {

    private static final String ENABLE_BUSINESS_DATE = "enable-business-date";

    private final CorevanceFeignClient corevanceClient;
    private final FeignGlobalConfigurationHelper configHelper;

    public FeignBusinessDateHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
        this.configHelper = new FeignGlobalConfigurationHelper(corevanceClient);
    }

    public BusinessDateResponse getBusinessDate(String type) {
        return ok(() -> corevanceClient.businessDateManagement().getBusinessDate(type));
    }

    public void updateBusinessDate(String type, String date) {
        updateBusinessDate(type, date, LoanTestData.ISO_DATE_PATTERN);
    }

    public void updateBusinessDate(String type, String date, String dateFormat) {
        BusinessDateUpdateRequest request = new BusinessDateUpdateRequest()//
                .type(BusinessDateUpdateRequest.TypeEnum.fromValue(type))//
                .date(date)//
                .dateFormat(dateFormat)//
                .locale(LoanTestData.LOCALE);

        ok(() -> corevanceClient.businessDateManagement().updateBusinessDate(request, Collections.emptyMap()));
    }

    public void runAt(String date, Runnable action) {
        runAt(date, LoanTestData.ISO_DATE_PATTERN, action);
    }

    public void runAt(String date, String dateFormat, Runnable action) {
        try {
            configHelper.updateConfigurationByName(ENABLE_BUSINESS_DATE, true);
            updateBusinessDate("BUSINESS_DATE", date, dateFormat);
            action.run();
        } finally {
            configHelper.updateConfigurationByName(ENABLE_BUSINESS_DATE, false);
        }
    }
}
