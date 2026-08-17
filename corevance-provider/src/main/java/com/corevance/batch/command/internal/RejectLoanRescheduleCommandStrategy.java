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
import com.corevance.portfolio.loanaccount.rescheduleloan.api.RescheduleLoansApiResource;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RejectLoanRescheduleCommandStrategy implements CommandStrategy {

    private final RescheduleLoansApiResource rescheduleLoansApiResource;

    @Override
    public BatchResponse execute(BatchRequest request, final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrlWithoutVersion(request));
        final Long scheduleId = Long.parseLong(pathParameters.get(1).substring(0, pathParameters.get(1).indexOf("?")));

        responseBody = rescheduleLoansApiResource.updateLoanRescheduleRequest(scheduleId, "reject", request.getBody());

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(responseBody);

        return response;
    }

}
