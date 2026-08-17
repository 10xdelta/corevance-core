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
package com.corevance.infrastructure.bulkimport.api;

import static com.corevance.util.StreamResponseUtil.DISPOSITION_TYPE_ATTACHMENT;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.bulkimport.data.GlobalEntityType;
import com.corevance.infrastructure.bulkimport.data.ImportData;
import com.corevance.infrastructure.bulkimport.exceptions.ImportTypeNotFoundException;
import com.corevance.infrastructure.bulkimport.service.BulkImportWorkbookService;
import com.corevance.infrastructure.core.api.ApiRequestParameterHelper;
import com.corevance.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import com.corevance.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.corevance.infrastructure.documentmanagement.data.DocumentData;
import com.corevance.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import com.corevance.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import com.corevance.util.StreamResponseUtil;
import org.springframework.stereotype.Component;

@Path("/v1/imports")
@Component
@Tag(name = "Bulk Import", description = "")
@RequiredArgsConstructor
public class BulkImportApiResource {

    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final DefaultToApiJsonSerializer<ImportData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveImportDocuments(@Context final UriInfo uriInfo, @QueryParam("entityType") final String entityType) {
        Collection<ImportData> importData = new ArrayList<>();

        if (entityType.equals(GlobalEntityType.CLIENT.getCode())) {
            final var importForClientEntity = this.bulkImportWorkbookService.getImports(GlobalEntityType.CLIENTS_ENTITY);
            final var importForClientPerson = this.bulkImportWorkbookService.getImports(GlobalEntityType.CLIENTS_PERSON);

            if (importForClientEntity != null) {
                importData.addAll(importForClientEntity);
            }

            if (importForClientPerson != null) {
                importData.addAll(importForClientPerson);
            }
        } else {
            final GlobalEntityType type = GlobalEntityType.fromCode(entityType);

            if (type == null) {
                throw new ImportTypeNotFoundException(entityType);
            }

            importData = this.bulkImportWorkbookService.getImports(type);
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, importData);
    }

    @GET
    @Path("getOutputTemplateLocation")
    public String retriveOutputTemplateLocation(@QueryParam("importDocumentId") final Long importDocumentId) {
        final var imporData = bulkImportWorkbookService.getImport(importDocumentId);

        return Optional.ofNullable(imporData)
                .flatMap(importData -> Optional.ofNullable(documentReadPlatformService.retrieveDocument(imporData.getDocumentId())))
                .map(DocumentData::getLocation).orElse(null);
    }

    @GET
    @Path("downloadOutputTemplate")
    @Produces("application/vnd.ms-excel")
    public Response getOutputTemplate(@QueryParam("importDocumentId") final Long importDocumentId) {
        final var importData = bulkImportWorkbookService.getImport(importDocumentId);
        if (importData == null) {
            throw new DocumentNotFoundException("IMPORT", importDocumentId, -1L);
        }
        final var doc = documentReadPlatformService.retrieveDocument(importData.getDocumentId());
        if (doc == null) {
            throw new DocumentNotFoundException("IMPORT", importDocumentId, importData.getDocumentId());
        }
        final var content = documentReadPlatformService.retrieveDocumentContent(doc.getParentEntityType(), doc.getParentEntityId(),
                doc.getId());
        final var streamResponseData = StreamResponseUtil.StreamResponseData.builder().type(content.getContentType())
                .fileName(content.getFileName()).stream(content.getStream()).dispositionType(DISPOSITION_TYPE_ATTACHMENT).build();

        return StreamResponseUtil.ok(streamResponseData);
    }
}
