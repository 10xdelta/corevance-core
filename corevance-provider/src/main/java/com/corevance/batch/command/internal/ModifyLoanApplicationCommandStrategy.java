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
import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.corevance.batch.command.CommandStrategy;
import com.corevance.batch.command.CommandStrategyUtils;
import com.corevance.batch.domain.BatchRequest;
import com.corevance.batch.domain.BatchResponse;
import com.corevance.portfolio.loanaccount.api.LoansApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link com.corevance.batch.command.CommandStrategy} to handle any updates to the loan application.
 * It passes the contents of the body from the BatchRequest to
 * {@link com.corevance.portfolio.loanaccount.api.LoansApiResource} and gets back the response. This class will
 * also catch any errors raised by {@link com.corevance.portfolio.loanaccount.api.LoansApiResource} and map those
 * errors to appropriate status codes in BatchResponse.
 *
 * @see com.corevance.batch.command.CommandStrategy
 * @see com.corevance.batch.domain.BatchRequest
 * @see com.corevance.batch.domain.BatchResponse
 */
@Component
@RequiredArgsConstructor
public class ModifyLoanApplicationCommandStrategy implements CommandStrategy {

    /**
     * {@link LoansApiResource} object
     */
    private final LoansApiResource loansApiResource;

    /**
     * Returns {@link com.corevance.batch.domain.BatchResponse} object by taking in and executing
     * {@link com.corevance.batch.domain.BatchRequest} object.
     *
     * @param request
     *            the {@link com.corevance.batch.domain.BatchRequest} object
     * @param uriInfo
     *            the {@link UriInfo} object
     * @return response the {@link com.corevance.batch.domain.BatchResponse} object
     */
    @Override
    public BatchResponse execute(final BatchRequest request, final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);

        // Get the loan id for use in loansApiResource
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);

        final String loanIdPathParameter = pathParameters.get(1);
        Long loanId;
        if (loanIdPathParameter.contains("?")) {
            loanId = Long.parseLong(loanIdPathParameter.substring(0, loanIdPathParameter.indexOf("?")));
        } else {
            loanId = Long.parseLong(loanIdPathParameter);
        }

        final Map<String, String> queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);
        final String command = queryParameters.get("command");

        responseBody = loansApiResource.modifyLoanApplication(loanId, command, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);

        // Sets the body of the response modifying the loan application
        response.setBody(responseBody);
        return response;
    }

}
