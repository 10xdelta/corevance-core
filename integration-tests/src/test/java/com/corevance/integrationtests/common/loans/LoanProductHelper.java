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
package com.corevance.integrationtests.common.loans;

import java.util.Collection;
import com.corevance.client.models.GetLoanProductsProductIdResponse;
import com.corevance.client.models.GetLoanProductsTemplateResponse;
import com.corevance.client.models.LoanProductBasicDetailsData;
import com.corevance.client.models.PostLoanProductsRequest;
import com.corevance.client.models.PostLoanProductsResponse;
import com.corevance.client.models.PutLoanProductsProductIdRequest;
import com.corevance.client.models.PutLoanProductsProductIdResponse;
import com.corevance.client.util.Calls;
import com.corevance.integrationtests.common.CorevanceClientHelper;

public class LoanProductHelper {

    public LoanProductHelper() {}

    public PostLoanProductsResponse createLoanProduct(PostLoanProductsRequest request) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().loanProducts.createLoanProduct(request));
    }

    public GetLoanProductsProductIdResponse retrieveLoanProductByExternalId(String externalId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().loanProducts.retrieveLoanProductDetailsByExternalId(externalId));
    }

    public GetLoanProductsProductIdResponse retrieveLoanProductById(Long loanProductId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().loanProducts.retrieveOneLoanProduct(loanProductId));
    }

    public PutLoanProductsProductIdResponse updateLoanProductByExternalId(String externalId, PutLoanProductsProductIdRequest request) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().loanProducts.updateLoanProductByExternalId(externalId, request));
    }

    public PutLoanProductsProductIdResponse updateLoanProductById(Long loanProductId, PutLoanProductsProductIdRequest request) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().loanProducts.updateLoanProduct(loanProductId, request));
    }

    public GetLoanProductsTemplateResponse getLoanProductTemplate(boolean isProductMixTemplate) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().loanProducts.retrieveTemplateLoanProduct(isProductMixTemplate));
    }

    public static Collection<LoanProductBasicDetailsData> fetchProductBasicDetailsList() {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().loanProductsDetails.retrieveAllLoanProductsDetails());
    }
}
