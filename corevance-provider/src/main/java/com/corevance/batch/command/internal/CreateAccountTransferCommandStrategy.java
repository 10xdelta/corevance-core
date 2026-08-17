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
import com.corevance.commands.domain.CommandWrapper;
import com.corevance.commands.service.CommandWrapperBuilder;
import com.corevance.commands.service.PortfolioCommandSourceWritePlatformService;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Batch command strategy for creating account transfers via {@code POST v1/accounttransfers}.
 *
 * The {@link com.corevance.portfolio.account.api.AccountTransfersApiResource#create} method accepts a typed DTO
 * and immediately re-serializes it to JSON before passing it to the command pipeline. This strategy bypasses that
 * round-trip and passes the raw request body directly to {@link PortfolioCommandSourceWritePlatformService}, which is
 * functionally equivalent.
 */
@Component
@RequiredArgsConstructor
public class CreateAccountTransferCommandStrategy implements CommandStrategy {

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<CommandProcessingResult> toApiJsonSerializer;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") final UriInfo uriInfo) {
        final BatchResponse response = new BatchResponse();
        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createAccountTransfer().withJson(request.getBody()).build();

        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        response.setStatusCode(HttpStatus.SC_OK);
        response.setBody(toApiJsonSerializer.serialize(result));

        return response;
    }
}
