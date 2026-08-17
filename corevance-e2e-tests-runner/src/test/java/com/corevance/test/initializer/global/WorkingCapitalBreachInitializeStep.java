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
package com.corevance.test.initializer.global;

import static com.corevance.client.feign.util.FeignCalls.executeVoid;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.WorkingCapitalBreachData;
import com.corevance.client.models.WorkingCapitalBreachRequest;
import com.corevance.client.models.WorkingCapitalNearBreachData;
import com.corevance.client.models.WorkingCapitalNearBreachRequest;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WorkingCapitalBreachInitializeStep implements CorevanceGlobalInitializerStep {

    public static final String DEFAULT_WC_BREACH_NAME = "Default Working Capital breach";
    public static final Integer DEFAULT_WC_BREACH_FREQUENCY = 2;
    public static final String DEFAULT_WC_BREACH_FREQUENCY_TYPE = "MONTHS";
    public static final String DEFAULT_WC_BREACH_AMOUNT_CALCULATION_TYPE = "PERCENTAGE";
    public static final BigDecimal DEFAULT_WC_BREACH_AMOUNT = new BigDecimal("1.23");

    public static final String DEFAULT_WC_NEAR_BREACH_NAME = "Default Working Capital near breach";
    public static final Integer DEFAULT_WC_NEAR_BREACH_FREQUENCY = 1;
    public static final String DEFAULT_WC_NEAR_BREACH_FREQUENCY_TYPE = "MONTHS";
    public static final BigDecimal DEFAULT_WC_NEAR_BREACH_THRESHOLD = new BigDecimal("72.15");

    private final CorevanceFeignClient corevanceClient;

    @Override
    public void initialize() {
        setDefaultWCBreach();
        setDefaultWCNearBreach();
    }

    public void setDefaultWCBreach() {
        try {
            List<WorkingCapitalBreachData> existingBuckets = corevanceClient.workingCapitalBreaches()
                    .retrieveAllWorkingCapitalBreaches(Map.of());
            boolean bucketExists = existingBuckets.stream().anyMatch(b -> DEFAULT_WC_BREACH_NAME.equals(b.getName()));

            if (bucketExists) {
                return;
            }
        } catch (Exception e) {
            log.debug("Could not retrieve existing working capital breaches, will create default breach", e);
        }

        WorkingCapitalBreachRequest defaultWorkingCapitalBreachRequest = new WorkingCapitalBreachRequest()//
                .name(DEFAULT_WC_BREACH_NAME) //
                .breachFrequency(DEFAULT_WC_BREACH_FREQUENCY) //
                .breachFrequencyType(DEFAULT_WC_BREACH_FREQUENCY_TYPE) //
                .breachAmountCalculationType(DEFAULT_WC_BREACH_AMOUNT_CALCULATION_TYPE) //
                .breachAmount(DEFAULT_WC_BREACH_AMOUNT);

        executeVoid(() -> corevanceClient.workingCapitalBreaches().createWorkingCapitalBreach(defaultWorkingCapitalBreachRequest, Map.of()));
    }

    public void setDefaultWCNearBreach() {
        try {
            List<WorkingCapitalNearBreachData> existingBuckets = corevanceClient.workingCapitalNearBreaches()
                    .retrieveAllWorkingCapitalNearBreaches(Map.of());
            boolean bucketExists = existingBuckets.stream().anyMatch(b -> DEFAULT_WC_NEAR_BREACH_NAME.equals(b.getName()));

            if (bucketExists) {
                return;
            }
        } catch (Exception e) {
            log.debug("Could not retrieve existing working capital breaches, will create default breach", e);
        }

        WorkingCapitalNearBreachRequest defaultWorkingCapitalNearBreachRequest = new WorkingCapitalNearBreachRequest()//
                .nearBreachName(DEFAULT_WC_NEAR_BREACH_NAME) //
                .nearBreachFrequency(DEFAULT_WC_NEAR_BREACH_FREQUENCY) //
                .nearBreachFrequencyType(DEFAULT_WC_NEAR_BREACH_FREQUENCY_TYPE) //
                .nearBreachThreshold(DEFAULT_WC_NEAR_BREACH_THRESHOLD); //

        executeVoid(() -> corevanceClient.workingCapitalNearBreaches().createWorkingCapitalNearBreach(defaultWorkingCapitalNearBreachRequest,
                Map.of()));
    }
}
