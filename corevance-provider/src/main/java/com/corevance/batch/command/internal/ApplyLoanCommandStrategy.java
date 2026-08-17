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
package com.corevance.batch.command.internal;

import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import com.corevance.batch.command.CommandStrategy;
import com.corevance.batch.domain.BatchRequest;
import com.corevance.batch.domain.BatchResponse;
import com.corevance.portfolio.loanaccount.api.LoansApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link com.corevance.batch.command.CommandStrategy} and applies a new loan on an existing client. It
 * passes the contents of the body from the BatchRequest to
 * {@link com.corevance.portfolio.loanaccount.api.LoansApiResource} and gets back the response. This class will
 * also catch any errors raised by {@link com.corevance.portfolio.loanaccount.api.LoansApiResource} and map those
 * errors to appropriate status codes in BatchResponse.
 *
 * @author Rishabh Shukla
 *
 * @see com.corevance.batch.command.CommandStrategy
 * @see com.corevance.batch.domain.BatchRequest
 * @see com.corevance.batch.domain.BatchResponse
 */
@Component
@RequiredArgsConstructor
public class ApplyLoanCommandStrategy implements CommandStrategy {

    private final LoansApiResource loansApiResource;

    @Override
    public BatchResponse execute(BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        // Calls 'SubmitLoanFunction' function from 'LoansApiResource' to
        // Apply Loan to an existing client
        responseBody = loansApiResource.calculateLoanScheduleOrSubmitLoanApplication(null, null, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        // Sets the body of the response after loan is successfully applied
        response.setBody(responseBody);

        return response;
    }
}
