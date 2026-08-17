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

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.CurrencyUpdateRequest;
import com.corevance.test.support.TestContext;
import com.corevance.test.support.TestContextKey;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CurrencyGlobalInitializerStep implements CorevanceGlobalInitializerStep {

    public static final List<String> CURRENCIES = Arrays.asList("EUR", "USD");

    private final CorevanceFeignClient corevanceClient;

    @Override
    public void initialize() {
        var request = new CurrencyUpdateRequest();
        var response = ok(() -> corevanceClient.currency().updateCurrencies(request.currencies(CURRENCIES), Map.of()));
        TestContext.INSTANCE.set(TestContextKey.PUT_CURRENCIES_RESPONSE, response);
    }
}
