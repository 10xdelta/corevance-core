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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.models.BusinessDateResponse;
import com.corevance.client.models.BusinessDateUpdateRequest;
import com.corevance.client.models.BusinessDateUpdateResponse;
import com.corevance.client.models.PutGlobalConfigurationsRequest;
import com.corevance.infrastructure.businessdate.domain.BusinessDateType;
import com.corevance.infrastructure.configuration.api.GlobalConfigurationConstants;

@Slf4j
public final class BusinessDateHelper {

    private static final String DATETIME_PATTERN = "dd MMMM yyyy";

    public BusinessDateHelper() {}

    public static BusinessDateUpdateResponse updateBusinessDate(final BusinessDateUpdateRequest request) {
        log.info("------------------UPDATE BUSINESS DATE----------------------");
        log.info("------------------Type: {}, date: {}----------------------", request.getType(), request.getDate());
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().businessDateManagement().updateBusinessDate(request));
    }

    public static BusinessDateUpdateResponse updateBusinessDate(final BusinessDateType type, final LocalDate date) {
        return updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.valueOf(type.name()))
                .date(Utils.dateFormatter.format(date)).dateFormat(Utils.DATE_FORMAT).locale("en"));
    }

    public BusinessDateResponse getBusinessDate(final String type) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().businessDateManagement().getBusinessDate(type));
    }

    public List<BusinessDateResponse> getBusinessDates() {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().businessDateManagement().getBusinessDates());
    }

    public static void runAt(String date, Runnable runnable) {
        try {
            new GlobalConfigurationHelper().updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            updateBusinessDate(new BusinessDateUpdateRequest().type(BusinessDateUpdateRequest.TypeEnum.BUSINESS_DATE).date(date)
                    .dateFormat(DATETIME_PATTERN).locale("en"));
            runnable.run();
        } finally {
            new GlobalConfigurationHelper().updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }
}
