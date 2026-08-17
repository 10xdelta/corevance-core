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
package com.corevance.portfolio.collateralmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.corevance.command.core.CommandDispatcher;
import com.corevance.infrastructure.core.annotation.AlternativeOperationId;
import com.corevance.organisation.monetary.data.CurrencyData;
import com.corevance.organisation.monetary.service.CurrencyReadPlatformService;
import com.corevance.portfolio.collateralmanagement.command.CollateralProductCreateCommand;
import com.corevance.portfolio.collateralmanagement.command.CollateralProductDeleteCommand;
import com.corevance.portfolio.collateralmanagement.command.CollateralProductUpdateCommand;
import com.corevance.portfolio.collateralmanagement.data.CollateralManagementData;
import com.corevance.portfolio.collateralmanagement.data.CollateralProductCreateRequest;
import com.corevance.portfolio.collateralmanagement.data.CollateralProductCreateResponse;
import com.corevance.portfolio.collateralmanagement.data.CollateralProductDeleteRequest;
import com.corevance.portfolio.collateralmanagement.data.CollateralProductDeleteResponse;
import com.corevance.portfolio.collateralmanagement.data.CollateralProductUpdateRequest;
import com.corevance.portfolio.collateralmanagement.data.CollateralProductUpdateResponse;
import com.corevance.portfolio.collateralmanagement.service.CollateralManagementReadService;
import org.springframework.stereotype.Component;

@Path("/v1/collateral-management")
@Component
@Tag(name = "Collateral Management", description = "Collateral Management is for managing collateral operations")
@RequiredArgsConstructor
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public class CollateralManagementApiResource {

    private final CommandDispatcher dispatcher;
    private final CollateralManagementReadService collateralManagementReadService;
    private final CurrencyReadPlatformService currencyReadPlatformService;

    @POST
    @Operation(summary = "Create a new collateral", description = "Collateral Creation")
    public CollateralProductCreateResponse createCollateral(@Valid CollateralProductCreateRequest request) {
        final var command = new CollateralProductCreateCommand();
        command.setPayload(request);
        return dispatcher.<CollateralProductCreateRequest, CollateralProductCreateResponse>dispatch(command).get();
    }

    @GET
    @Path("{collateralId}")
    @Operation(summary = "Get Collateral", description = "Fetch Collateral")
    public CollateralManagementData getCollateral(
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        return collateralManagementReadService.getCollateralProduct(collateralId);
    }

    @GET
    @Operation(summary = "Get All Collaterals", description = "Fetch all Collateral Products")
    public List<CollateralManagementData> getAllCollaterals() {
        return collateralManagementReadService.getAllCollateralProducts();
    }

    @GET
    @Path("template")
    @Operation(summary = "Get Collateral Template", description = "Get Collateral Template")
    public List<CurrencyData> getCollateralTemplate() {
        return currencyReadPlatformService.retrieveAllPlatformCurrencies();
    }

    @PUT
    @Path("{collateralId}")
    @Operation(operationId = "updateCollateral_1", summary = "Update Collateral", description = "Update Collateral")
    @AlternativeOperationId("updateCollateral_2")
    public CollateralProductUpdateResponse updateCollateral(
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Valid CollateralProductUpdateRequest request) {
        request.setCollateralId(collateralId);
        final var command = new CollateralProductUpdateCommand();
        command.setPayload(request);
        return dispatcher.<CollateralProductUpdateRequest, CollateralProductUpdateResponse>dispatch(command).get();
    }

    @DELETE
    @Path("{collateralId}")
    @Operation(operationId = "deleteCollateral_1", summary = "Delete a Collateral", description = "Delete Collateral")
    @AlternativeOperationId("deleteCollateral_2")
    public CollateralProductDeleteResponse deleteCollateral(
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        final var request = CollateralProductDeleteRequest.builder().collateralId(collateralId).build();
        final var command = new CollateralProductDeleteCommand();
        command.setPayload(request);
        return dispatcher.<CollateralProductDeleteRequest, CollateralProductDeleteResponse>dispatch(command).get();
    }
}
