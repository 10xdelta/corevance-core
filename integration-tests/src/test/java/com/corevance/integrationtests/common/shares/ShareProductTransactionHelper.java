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
package com.corevance.integrationtests.common.shares;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.Map;
import com.corevance.integrationtests.common.Utils;

public final class ShareProductTransactionHelper {

    private ShareProductTransactionHelper() {

    }

    private static final String SHARE_PRODUCT_URL = "/corevance-provider/api/v1/products/share";
    private static final String CREATE_SHARE_PRODUCT_URL = SHARE_PRODUCT_URL + "?" + Utils.TENANT_IDENTIFIER;

    // TODO: Rewrite to use corevance-client instead!
    // Example: com.corevance.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // com.corevance.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer createShareProduct(final String savingsProductJSON, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_SHARE_PRODUCT_URL, savingsProductJSON, "resourceId");
    }

    // TODO: Rewrite to use corevance-client instead!
    // Example: com.corevance.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // com.corevance.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Map<String, Object> retrieveShareProduct(final Integer shareProductId, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        String url = SHARE_PRODUCT_URL + "/" + shareProductId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpec, url, "");
    }

    // TODO: Rewrite to use corevance-client instead!
    // Example: com.corevance.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // com.corevance.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public static Integer updateShareProduct(final Integer shareProductId, final String provsioningCriteriaJson,
            final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        String url = SHARE_PRODUCT_URL + "/" + shareProductId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerPut(requestSpec, responseSpec, url, provsioningCriteriaJson, "resourceId");
    }
}
