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

import static com.corevance.batch.command.CommandStrategyUtils.relativeUrlWithoutVersion;

import com.google.common.base.Splitter;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.corevance.batch.command.CommandStrategy;
import com.corevance.batch.domain.BatchRequest;
import com.corevance.batch.domain.BatchResponse;
import com.corevance.portfolio.loanaccount.api.LoanChargesApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link com.corevance.batch.command.CommandStrategy} and Collect Charges for a Loan. It passes the
 * contents of the body from the BatchRequest to
 * {@link com.corevance.portfolio.loanaccount.api.LoanChargesApiResource} and gets back the response. This class
 * will also catch any errors raised by {@link com.corevance.portfolio.loanaccount.api.LoanChargesApiResource} and
 * map those errors to appropriate status codes in BatchResponse.
 *
 * @see com.corevance.batch.command.CommandStrategy
 * @see com.corevance.batch.domain.BatchRequest
 * @see com.corevance.batch.domain.BatchResponse
 */
@Component
@RequiredArgsConstructor
public class CollectChargesCommandStrategy implements CommandStrategy {

    /**
     * Loan charges api resource {@link LoanChargesApiResource}.
     */
    private final LoanChargesApiResource loanChargesApiResource;

    @Override
    public BatchResponse execute(BatchRequest request, final UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrlWithoutVersion(request));

        // Pluck out the loanId out of the relative path
        final Long loanId = Long.parseLong(pathParameters.get(1));

        // Calls 'retrieveAllLoanCharges' function from
        // 'LoanChargesApiResource' to Collect
        // Charges for a loan
        responseBody = loanChargesApiResource.retrieveAllLoanCharges(loanId, uriInfo);

        response.setStatusCode(HttpStatus.SC_OK);
        // Sets the body of the response after Charges have been
        // successfully collected
        response.setBody(responseBody);

        return response;
    }
}
