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
package com.corevance.infrastructure.campaigns.email.api;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import com.corevance.commands.domain.CommandWrapper;
import com.corevance.commands.service.CommandWrapperBuilder;
import com.corevance.commands.service.PortfolioCommandSourceWritePlatformService;
import com.corevance.infrastructure.campaigns.email.data.EmailConfigurationData;
import com.corevance.infrastructure.campaigns.email.service.EmailConfigurationReadPlatformService;
import com.corevance.infrastructure.core.annotation.AlternativeOperationId;
import com.corevance.infrastructure.core.api.ApiRequestParameterHelper;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.corevance.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/email/configuration")
@Produces({ MediaType.APPLICATION_JSON })
@Component
@RequiredArgsConstructor
public class EmailConfigurationApiResource {

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "EMAIL_CONFIGURATION";
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<EmailConfigurationData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final EmailConfigurationReadPlatformService emailConfigurationReadPlatformService;

    @GET
    @Operation(summary = "List all email configurations", operationId = "retrieveAllEmailConfigurations")
    @AlternativeOperationId("retrieveAll_5")
    public String retrieveAll(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<EmailConfigurationData> configuration = this.emailConfigurationReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, configuration);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update email configuration", operationId = "updateEmailConfiguration")
    @AlternativeOperationId("updateConfiguration")
    public String updateConfiguration(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateEmailConfiguration().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
