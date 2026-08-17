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
package com.corevance.test.stepdef.loan;

import static com.corevance.client.feign.util.FeignCalls.executeVoid;

import io.cucumber.java.en.When;
import java.io.IOException;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.PostLoansResponse;
import com.corevance.test.stepdef.AbstractStepDef;
import com.corevance.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;

public class LoanReprocessStepDef extends AbstractStepDef {

    @Autowired
    private CorevanceFeignClient corevanceClient;

    @When("Admin runs loan reprocess for Loan")
    public void admin_runs_inline_COB_job_for_loan() throws IOException {
        PostLoansResponse loanResponse = testContext().get(TestContextKey.LOAN_CREATE_RESPONSE);
        long loanId = loanResponse.getLoanId();

        executeVoid(() -> corevanceClient.internalCob().loanReprocess(loanId));
    }
}
