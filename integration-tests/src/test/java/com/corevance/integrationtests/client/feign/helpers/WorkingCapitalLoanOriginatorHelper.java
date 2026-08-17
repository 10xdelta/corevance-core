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

import com.corevance.client.feign.services.LoanOriginatorsApi;
import com.corevance.client.feign.services.WorkingCapitalLoanOriginatorsApi;
import com.corevance.client.feign.util.FeignCalls;
import com.corevance.client.models.LoanOriginatorMappingResponse;
import com.corevance.client.models.LoanOriginatorsResponse;
import com.corevance.client.models.PostLoanOriginatorsRequest;
import com.corevance.client.models.PostLoanOriginatorsResponse;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;

public class WorkingCapitalLoanOriginatorHelper {

    private static LoanOriginatorsApi api() {
        return CorevanceFeignClientHelper.getCorevanceFeignClient().loanOriginators();
    }

    private static WorkingCapitalLoanOriginatorsApi workingCapitalLoanOriginatorsApi() {
        return CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanOriginators();
    }

    private static final String WORKING_CAPITAL_LOAN_ORIGINATOR_API_URL = "/corevance-provider/api/v1/working-capital-loans";

    public Long createOriginator(final String externalId, final String name) {
        PostLoanOriginatorsRequest request = new PostLoanOriginatorsRequest();
        request.setExternalId(externalId);
        request.setName(name);
        request.setStatus("ACTIVE");

        PostLoanOriginatorsResponse response = FeignCalls.ok(() -> api().createLoanOriginator(request));
        return response.getResourceId();
    }

    public void deleteOriginator(final Long originatorId) {
        FeignCalls.ok(() -> api().deleteLoanOriginator(originatorId));
    }

    public LoanOriginatorMappingResponse attachOriginatorToWorkingCapitalLoan(final Long loanId, final Long originatorId) {
        return FeignCalls.ok(() -> workingCapitalLoanOriginatorsApi().attachOriginatorToWorkingCapitalLoan(loanId, originatorId));
    }

    public LoanOriginatorMappingResponse detachOriginatorFromWorkingCapitalLoan(final Long loanId, final Long originatorId) {
        return FeignCalls.ok(() -> workingCapitalLoanOriginatorsApi().detachOriginatorFromWorkingCapitalLoan(loanId, originatorId));
    }

    public LoanOriginatorsResponse retrieveOriginatorsByWorkingCapitalLoanId(final Long loanId) {
        return FeignCalls.ok(() -> workingCapitalLoanOriginatorsApi().retrieveOriginatorsByWorkingCapitalLoanId(loanId));
    }
}
