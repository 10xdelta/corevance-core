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
import org.apache.commons.lang3.StringUtils;
import com.corevance.batch.command.CommandStrategy;
import com.corevance.batch.domain.BatchRequest;
import com.corevance.batch.domain.BatchResponse;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.corevance.portfolio.workingcapitalloan.api.WorkingCapitalLoanTransactionsApiResource;
import com.corevance.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetWorkingCapitalLoanTransactionByExternalIdCommandStrategy implements CommandStrategy {

    private final WorkingCapitalLoanTransactionsApiResource workingCapitalLoanTransactionsApiResource;
    private final DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

    @Override
    public BatchResponse execute(BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);

        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);
        final String loanExternalId = pathParameters.get(2);
        String transactionExternalId;
        if (relativeUrl.indexOf('?') > 0) {
            transactionExternalId = StringUtils.substringBeforeLast(pathParameters.get(5), "?");
        } else {
            transactionExternalId = pathParameters.get(5);
        }

        final WorkingCapitalLoanTransactionData workingCapitalLoanTransactionData = workingCapitalLoanTransactionsApiResource
                .retrieveTransactionByExternalLoanIdAndTransactionExternalId(loanExternalId, transactionExternalId);

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(toApiJsonSerializer.serialize(workingCapitalLoanTransactionData));

        return response;
    }
}
