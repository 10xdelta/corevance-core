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
package com.corevance.integrationtests.common.workingcapitalloanproduct;

import java.util.List;
import com.corevance.client.feign.util.FeignCalls;
import com.corevance.client.models.DeleteWorkingCapitalLoanProductsProductIdResponse;
import com.corevance.client.models.GetWorkingCapitalLoanProductsProductIdResponse;
import com.corevance.client.models.GetWorkingCapitalLoanProductsResponse;
import com.corevance.client.models.GetWorkingCapitalLoanProductsTemplateResponse;
import com.corevance.client.models.PostWorkingCapitalLoanProductsRequest;
import com.corevance.client.models.PostWorkingCapitalLoanProductsResponse;
import com.corevance.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import com.corevance.client.models.PutWorkingCapitalLoanProductsProductIdResponse;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;

public class WorkingCapitalLoanProductHelper {

    public WorkingCapitalLoanProductHelper() {}

    public PostWorkingCapitalLoanProductsResponse createWorkingCapitalLoanProduct(final PostWorkingCapitalLoanProductsRequest request) {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .createWorkingCapitalLoanProduct(request));
    }

    public GetWorkingCapitalLoanProductsProductIdResponse retrieveWorkingCapitalLoanProductByExternalId(final String externalId) {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .retrieveOneWorkingCapitalLoanProductByExternalId(externalId));
    }

    public GetWorkingCapitalLoanProductsProductIdResponse retrieveWorkingCapitalLoanProductById(final Long productId) {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .retrieveOneWorkingCapitalLoanProduct(productId));
    }

    public List<GetWorkingCapitalLoanProductsResponse> retrieveAllWorkingCapitalLoanProducts() {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .retrieveAllWorkingCapitalLoanProducts());
    }

    public GetWorkingCapitalLoanProductsTemplateResponse retrieveTemplate() {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .retrieveTemplateWorkingCapitalLoanProduct());
    }

    public PutWorkingCapitalLoanProductsProductIdResponse updateWorkingCapitalLoanProductByExternalId(final String externalId,
            final PutWorkingCapitalLoanProductsProductIdRequest request) {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .updateWorkingCapitalLoanProductByExternalId(externalId, request));
    }

    public PutWorkingCapitalLoanProductsProductIdResponse updateWorkingCapitalLoanProductById(final Long productId,
            final PutWorkingCapitalLoanProductsProductIdRequest request) {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .updateWorkingCapitalLoanProduct(productId, request));
    }

    public DeleteWorkingCapitalLoanProductsProductIdResponse deleteWorkingCapitalLoanProductByExternalId(final String externalId) {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .deleteWorkingCapitalLoanProductByExternalId(externalId));
    }

    public DeleteWorkingCapitalLoanProductsProductIdResponse deleteWorkingCapitalLoanProductById(final Long productId) {
        return FeignCalls.ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanProducts()
                .deleteWorkingCapitalLoanProduct(productId));
    }
}
