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
package com.corevance.integrationtests.common.workingcapitalloannearbreach;

import java.util.List;
import com.corevance.client.feign.services.WorkingCapitalLoanBreachScheduleApi;
import com.corevance.client.feign.services.WorkingCapitalLoanNearBreachActionsApi;
import com.corevance.client.feign.util.CallFailedRuntimeException;
import com.corevance.client.feign.util.FeignCalls;
import com.corevance.client.models.PostWorkingCapitalLoansLoanIdNearBreachActionsRequest;
import com.corevance.client.models.WorkingCapitalLoanBreachScheduleData;
import com.corevance.client.models.WorkingCapitalLoanNearBreachActionData;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;

public class WorkingCapitalLoanNearBreachActionsHelper {

    public WorkingCapitalLoanNearBreachActionsHelper() {}

    private static WorkingCapitalLoanNearBreachActionsApi api() {
        return CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanNearBreachActions();
    }

    private static WorkingCapitalLoanBreachScheduleApi breachScheduleApi() {
        return CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanBreachSchedule();
    }

    public void createNearBreachActionById(final Long loanId, final PostWorkingCapitalLoansLoanIdNearBreachActionsRequest request) {
        FeignCalls.ok(() -> api().createWorkingCapitalLoanNearBreachActionById(loanId, request));
    }

    public void createNearBreachActionByExternalId(final String externalId,
            final PostWorkingCapitalLoansLoanIdNearBreachActionsRequest request) {
        FeignCalls.ok(() -> api().createWorkingCapitalLoanNearBreachActionByExternalId(externalId, request));
    }

    public CallFailedRuntimeException createNearBreachActionByIdExpectingFailure(final Long loanId,
            final PostWorkingCapitalLoansLoanIdNearBreachActionsRequest request) {
        return FeignCalls.fail(() -> api().createWorkingCapitalLoanNearBreachActionById(loanId, request));
    }

    public List<WorkingCapitalLoanBreachScheduleData> getBreachSchedule(final Long loanId) {
        return FeignCalls.ok(() -> breachScheduleApi().retrieveBreachSchedule(loanId));
    }

    public List<WorkingCapitalLoanNearBreachActionData> getNearBreachChangeActionsById(final Long loanId) {
        return FeignCalls.ok(() -> api().getWorkingCapitalLoanNearBreachActionsById(loanId));
    }

    public List<WorkingCapitalLoanNearBreachActionData> getNearBreachChangeActionsByExternalId(final String externalId) {
        return FeignCalls.ok(() -> api().getWorkingCapitalLoanNearBreachActionsByExternalId(externalId));
    }
}
