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
import com.corevance.client.models.GetSavingsProductsProductIdResponse;
import com.corevance.client.models.PostSavingsProductsRequest;
import com.corevance.client.models.PostSavingsProductsResponse;
import com.corevance.integrationtests.client.feign.modules.SavingsRequestBuilders;

public class FeignSavingsProductHelper {

    private final CorevanceFeignClient corevanceClient;

    public FeignSavingsProductHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public PostSavingsProductsResponse createSavingsProduct(PostSavingsProductsRequest request) {
        return ok(() -> corevanceClient.savingsProduct().createSavingsProduct(request));
    }

    public PostSavingsProductsResponse createDefaultSavingsProduct() {
        return createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct());
    }

    public GetSavingsProductsProductIdResponse getSavingsProduct(Long productId) {
        return ok(() -> corevanceClient.savingsProduct().retrieveOneSavingsProduct(productId));
    }
}
