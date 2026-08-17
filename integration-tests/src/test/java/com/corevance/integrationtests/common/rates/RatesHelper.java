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
package com.corevance.integrationtests.common.rates;

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.util.List;
import com.corevance.client.models.CommandProcessingResult;
import com.corevance.client.models.RateData;
import com.corevance.client.models.RateRequest;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;
import com.corevance.integrationtests.common.Utils;

public final class RatesHelper {

    private RatesHelper() {

    }

    private static final BigDecimal PERCENTAGE = BigDecimal.valueOf(10);
    private static final Integer PRODUCT_APPLY_LOAN = 1;
    private static final Boolean ACTIVE = true;

    public static List<RateData> getRates() {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().rate().retrieveAllRates());
    }

    public static CommandProcessingResult createRates(final RateRequest request) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().rate().createRate(request));
    }

    public static RateData getRateById(final Long rateId) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().rate().retrieveOneRate(rateId));
    }

    public static CommandProcessingResult updateRates(final Long rateId, final RateRequest request) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().rate().updateRate(rateId, request));
    }

    public static RateRequest getLoanRateRequest() {
        return getLoanRateRequest(RatesHelper.PRODUCT_APPLY_LOAN, RatesHelper.PERCENTAGE);
    }

    public static RateRequest getLoanRateRequest(final Integer productApply, final BigDecimal percentage) {
        return populateDefaultsForLoan().percentage(percentage).productApply(productApply);
    }

    public static RateRequest populateDefaultsForLoan() {
        return new RateRequest().active(RatesHelper.ACTIVE).percentage(RatesHelper.PERCENTAGE).locale("en")
                .productApply(RatesHelper.PRODUCT_APPLY_LOAN).name(Utils.uniqueRandomStringGenerator("Rate_Loans_", 6));
    }

    public static RateRequest getModifyRateRequest() {
        return new RateRequest().percentage(BigDecimal.valueOf(15.0)).locale("en");
    }

}
