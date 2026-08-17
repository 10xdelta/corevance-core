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
package com.corevance.integrationtests.common.accounting;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import com.corevance.client.models.DeleteFinancialActivityAccountsResponse;
import com.corevance.client.models.GetFinancialActivityAccountsResponse;
import com.corevance.client.models.PostFinancialActivityAccountsRequest;
import com.corevance.client.models.PostFinancialActivityAccountsResponse;
import com.corevance.client.util.Calls;
import com.corevance.integrationtests.common.CorevanceClientHelper;
import com.corevance.integrationtests.common.Utils;

@SuppressWarnings("rawtypes")
public class FinancialActivityAccountHelper {

    private static final String FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL = "/corevance-provider/api/v1/financialactivityaccounts";
    private final RequestSpecification requestSpec;

    public FinancialActivityAccountHelper(final RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    // TODO: Rewrite to use corevance-client instead!
    // Example: com.corevance.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // com.corevance.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Object createFinancialActivityAccount(Integer financialActivityId, Integer glAccountId,
            final ResponseSpecification responseSpecification, String jsonBack) {
        String json = FinancialActivityAccountsMappingBuilder.build(financialActivityId, glAccountId);
        return Utils.performServerPost(this.requestSpec, responseSpecification,
                FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "?" + Utils.TENANT_IDENTIFIER, json, jsonBack);
    }

    // TODO: Rewrite to use corevance-client instead!
    // Example: com.corevance.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // com.corevance.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Object updateFinancialActivityAccount(Integer financialActivityAccountId, Integer financialActivityId, Integer glAccountId,
            final ResponseSpecification responseSpecification, String jsonBack) {
        String json = FinancialActivityAccountsMappingBuilder.build(financialActivityId, glAccountId);
        return Utils.performServerPut(this.requestSpec, responseSpecification,
                FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/" + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER, json, jsonBack);
    }

    // TODO: Rewrite to use corevance-client instead!
    // Example: com.corevance.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // com.corevance.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public HashMap getFinancialActivityAccount(final Integer financialActivityAccountId,
            final ResponseSpecification responseSpecification) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/" + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(requestSpec, responseSpecification, url, "");
    }

    // TODO: Rewrite to use corevance-client instead!
    // Example: com.corevance.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // com.corevance.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public List<HashMap> getAllFinancialActivityAccounts(final ResponseSpecification responseSpecification) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerGet(this.requestSpec, responseSpecification, url, "");
    }

    // TODO: Rewrite to use corevance-client instead!
    // Example: com.corevance.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // com.corevance.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public Integer deleteFinancialActivityAccount(final Integer financialActivityAccountId,
            final ResponseSpecification responseSpecification, String jsonBack) {
        final String url = FINANCIAL_ACTIVITY_ACCOUNT_MAPPING_URL + "/" + financialActivityAccountId + "?" + Utils.TENANT_IDENTIFIER;
        return Utils.performServerDelete(this.requestSpec, responseSpecification, url, jsonBack);
    }

    public PostFinancialActivityAccountsResponse createFinancialActivityAccount(PostFinancialActivityAccountsRequest request) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().financialActivyAccountMappings
                .createGLAccountMappingFinancialActivityAccount(request));
    }

    public List<GetFinancialActivityAccountsResponse> getAllFinancialActivityAccounts() {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().financialActivyAccountMappings.retrieveAll());
    }

    public DeleteFinancialActivityAccountsResponse deleteFinancialActivityAccount(Long financialMappingId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().financialActivyAccountMappings
                .deleteGLAccountMappingFinancialActivityAccount(financialMappingId));
    }
}
