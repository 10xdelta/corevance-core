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
import org.apache.commons.lang3.StringUtils;
import com.corevance.batch.command.CommandStrategy;
import com.corevance.batch.command.CommandStrategyUtils;
import com.corevance.batch.domain.BatchRequest;
import com.corevance.batch.domain.BatchResponse;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.corevance.portfolio.workingcapitalloan.api.WorkingCapitalLoanApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModifyWorkingCapitalLoanApplicationByExternalIdCommandStrategy implements CommandStrategy {

    private final WorkingCapitalLoanApiResource workingCapitalLoanApiResource;
    private final DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

    @Override
    public BatchResponse execute(BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final String relativeUrl = relativeUrlWithoutVersion(request);

        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrl);

        final String loanIdPathParameter = pathParameters.get(2);
        String loanExternalId;
        if (loanIdPathParameter.contains("?")) {
            loanExternalId = StringUtils.substringBefore(loanIdPathParameter, "?");
        } else {
            loanExternalId = loanIdPathParameter;
        }

        final Map<String, String> queryParameters = CommandStrategyUtils.getQueryParameters(relativeUrl);
        final String command = queryParameters.get("command");

        final CommandProcessingResult commandProcessingResult = workingCapitalLoanApiResource
                .modifyLoanApplicationByExternalId(loanExternalId, command, request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(toApiJsonSerializer.serialize(commandProcessingResult));

        return response;
    }
}
