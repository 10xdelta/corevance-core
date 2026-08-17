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
package com.corevance.spm.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.annotation.AlternativeOperationId;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.portfolio.client.domain.Client;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.spm.data.ScorecardData;
import com.corevance.spm.domain.Scorecard;
import com.corevance.spm.domain.Survey;
import com.corevance.spm.service.ScorecardReadPlatformService;
import com.corevance.spm.service.ScorecardService;
import com.corevance.spm.service.SpmService;
import com.corevance.spm.util.ScorecardMapper;
import com.corevance.useradministration.domain.AppUser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Path("/v1/surveys/scorecards")
@Component
@Tag(name = "Score Card", description = "")
@RequiredArgsConstructor
public class ScorecardApiResource {

    private final PlatformSecurityContext securityContext;
    private final SpmService spmService;
    private final ScorecardService scorecardService;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ScorecardReadPlatformService scorecardReadPlatformService;

    @GET
    @Path("{surveyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    @Operation(summary = "List all Scorecard entries", description = "List all Scorecard entries for a survey.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Scorecard.class)))) })
    public List<ScorecardData> findBySurvey(@PathParam("surveyId") @Parameter(description = "Enter surveyId") final Long surveyId) {
        this.securityContext.authenticatedUser();
        this.spmService.findById(surveyId);
        return (List<ScorecardData>) this.scorecardReadPlatformService.retrieveScorecardBySurvey(surveyId);
    }

    @POST
    @Path("{surveyId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    @Operation(operationId = "createScorecard", summary = "Create a Scorecard entry", description = "Add a new entry to a survey.\n" + "\n"
            + "Mandatory Fields\n" + "clientId, createdOn, questionId, responseId, staffId")
    @AlternativeOperationId("createScorecard_1")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public void createScorecard(@PathParam("surveyId") @Parameter(description = "Enter surveyId") final Long surveyId,
            @Parameter(description = "scorecardData") final ScorecardData scorecardData) {
        final AppUser appUser = this.securityContext.authenticatedUser();
        final Survey survey = this.spmService.findById(surveyId);
        final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(scorecardData.getClientId());
        this.scorecardService.createScorecard(ScorecardMapper.map(scorecardData, survey, appUser, client));
    }

    @GET
    @Path("{surveyId}/clients/{clientId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    public List<ScorecardData> findBySurveyAndClient(@PathParam("surveyId") @Parameter(description = "Enter surveyId") final Long surveyId,
            @PathParam("clientId") @Parameter(description = "Enter clientId") final Long clientId) {
        this.securityContext.authenticatedUser();
        this.spmService.findById(surveyId);
        this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        return (List<ScorecardData>) this.scorecardReadPlatformService.retrieveScorecardBySurveyAndClient(surveyId, clientId);

    }

    @GET
    @Path("clients/{clientId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Transactional
    @Operation(operationId = "findByClient")
    @AlternativeOperationId("findByClient_1")
    public List<ScorecardData> findByClient(@PathParam("clientId") final Long clientId) {
        this.securityContext.authenticatedUser();
        this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
        return (List<ScorecardData>) this.scorecardReadPlatformService.retrieveScorecardByClient(clientId);
    }
}
