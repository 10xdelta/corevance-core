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
package com.corevance.infrastructure.entityaccess.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import com.corevance.commands.domain.CommandWrapper;
import com.corevance.commands.service.CommandWrapperBuilder;
import com.corevance.commands.service.PortfolioCommandSourceWritePlatformService;
import com.corevance.infrastructure.core.annotation.AlternativeOperationId;
import com.corevance.infrastructure.core.api.ApiRequestParameterHelper;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.corevance.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.corevance.infrastructure.entityaccess.data.CorevanceEntityRelationData;
import com.corevance.infrastructure.entityaccess.data.CorevanceEntityToEntityMappingData;
import com.corevance.infrastructure.entityaccess.service.CorevanceEntityAccessReadService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Component;

@Path("/v1/entitytoentitymapping")
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Corevance Entity", description = "")
@RequiredArgsConstructor
public class CorevanceEntityApiResource {

    private final PlatformSecurityContext context;
    private final CorevanceEntityAccessReadService readPlatformService;
    private final DefaultToApiJsonSerializer<CorevanceEntityRelationData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<CorevanceEntityToEntityMappingData> toApiJsonSerializerOfficeToLoanProducts;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveAll_3")
    @AlternativeOperationId("retrieveAll_7")
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(CorevanceEntityApiResourceConstants.COREVANCE_ENTITY_RESOURCE_NAME);

        final Collection<CorevanceEntityRelationData> entityMappings = this.readPlatformService.retrieveAllSupportedMappingTypes();
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, entityMappings, CorevanceEntityApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/{mapId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "retrieveOne")
    @AlternativeOperationId("retrieveOne_4")
    public String retrieveOne(@PathParam("mapId") final Long mapId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(CorevanceEntityApiResourceConstants.COREVANCE_ENTITY_RESOURCE_NAME);

        final Collection<CorevanceEntityToEntityMappingData> entityToEntityMappings = this.readPlatformService.retrieveOneMapping(mapId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerOfficeToLoanProducts.serialize(settings, entityToEntityMappings,
                CorevanceEntityApiResourceConstants.FETCH_ENTITY_TO_ENTITY_MAPPINGS);
    }

    @GET
    @Path("/{mapId}/{fromId}/{toId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String getEntityToEntityMappings(@PathParam("mapId") final Long mapId, @PathParam("fromId") final Long fromId,
            @PathParam("toId") final Long toId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(CorevanceEntityApiResourceConstants.COREVANCE_ENTITY_RESOURCE_NAME);

        final Collection<CorevanceEntityToEntityMappingData> entityToEntityMappings = this.readPlatformService
                .retrieveEntityToEntityMappings(mapId, fromId, toId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerOfficeToLoanProducts.serialize(settings, entityToEntityMappings,
                CorevanceEntityApiResourceConstants.FETCH_ENTITY_TO_ENTITY_MAPPINGS);
    }

    @POST
    @Path("/{relId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createMap(@PathParam("relId") final Long relId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createMap(relId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("/{mapId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateMap(@PathParam("mapId") final Long mapId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateMap(mapId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @DELETE
    @Path("{mapId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(operationId = "delete")
    @AlternativeOperationId("delete_4")
    public String delete(@PathParam("mapId") final Long mapId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteMap(mapId) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
