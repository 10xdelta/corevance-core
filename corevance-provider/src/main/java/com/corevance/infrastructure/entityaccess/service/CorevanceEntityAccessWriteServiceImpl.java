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
package com.corevance.infrastructure.entityaccess.service;

import jakarta.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.Map;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.corevance.infrastructure.codes.domain.CodeValue;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.data.CommandProcessingResultBuilder;
import com.corevance.infrastructure.core.exception.ErrorHandler;
import com.corevance.infrastructure.core.exception.PlatformDataIntegrityException;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.infrastructure.entityaccess.api.CorevanceEntityApiResourceConstants;
import com.corevance.infrastructure.entityaccess.data.CorevanceEntityDataValidator;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityAccess;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityAccessRepository;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityRelation;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityRelationRepositoryWrapper;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityToEntityMapping;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityToEntityMappingRepository;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityToEntityMappingRepositoryWrapper;
import com.corevance.infrastructure.entityaccess.exception.CorevanceEntityToEntityMappingDateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CorevanceEntityAccessWriteServiceImpl implements CorevanceEntityAccessWriteService {

    private static final Logger LOG = LoggerFactory.getLogger(CorevanceEntityAccessWriteServiceImpl.class);
    private final CorevanceEntityAccessRepository entityAccessRepository;
    private final CorevanceEntityRelationRepositoryWrapper corevanceEntityRelationRepositoryWrapper;
    private final CorevanceEntityToEntityMappingRepository corevanceEntityToEntityMappingRepository;
    private final CorevanceEntityToEntityMappingRepositoryWrapper corevanceEntityToEntityMappingRepositoryWrapper;
    private final CorevanceEntityDataValidator fromApiJsonDeserializer;

    @Autowired
    public CorevanceEntityAccessWriteServiceImpl(final CorevanceEntityAccessRepository entityAccessRepository,
            final CorevanceEntityRelationRepositoryWrapper corevanceEntityRelationRepositoryWrapper,
            final CorevanceEntityToEntityMappingRepository corevanceEntityToEntityMappingRepository,
            final CorevanceEntityToEntityMappingRepositoryWrapper corevanceEntityToEntityMappingRepositoryWrapper,
            CorevanceEntityDataValidator fromApiJsonDeserializer) {
        this.entityAccessRepository = entityAccessRepository;
        this.corevanceEntityToEntityMappingRepository = corevanceEntityToEntityMappingRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.corevanceEntityRelationRepositoryWrapper = corevanceEntityRelationRepositoryWrapper;
        this.corevanceEntityToEntityMappingRepositoryWrapper = corevanceEntityToEntityMappingRepositoryWrapper;
    }

    @Override
    public CommandProcessingResult createEntityAccess(@SuppressWarnings("unused") JsonCommand command) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @Transactional
    public void addNewEntityAccess(final String entityType, final Long entityId, final CodeValue accessType, final String secondEntityType,
            final Long secondEntityId) {
        CorevanceEntityAccess entityAccess = CorevanceEntityAccess.createNew(entityType, entityId, accessType, secondEntityType,
                secondEntityId);
        entityAccessRepository.save(entityAccess);
    }

    @Override
    @Transactional
    public CommandProcessingResult createEntityToEntityMapping(Long relId, JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final CorevanceEntityRelation mapId = this.corevanceEntityRelationRepositoryWrapper.findOneWithNotFoundDetection(relId);

            final Long fromId = command.longValueOfParameterNamed(CorevanceEntityApiResourceConstants.fromEnityType);
            final Long toId = command.longValueOfParameterNamed(CorevanceEntityApiResourceConstants.toEntityType);
            final LocalDate startDate = command.localDateValueOfParameterNamed(CorevanceEntityApiResourceConstants.startDate);
            final LocalDate endDate = command.localDateValueOfParameterNamed(CorevanceEntityApiResourceConstants.endDate);

            fromApiJsonDeserializer.checkForEntity(relId.toString(), fromId, toId);
            if (endDate != null && DateUtils.isBefore(endDate, startDate)) {
                throw new CorevanceEntityToEntityMappingDateException(startDate.toString(), endDate.toString());
            }

            final CorevanceEntityToEntityMapping newMap = CorevanceEntityToEntityMapping.newMap(mapId, fromId, toId, startDate, endDate);

            this.corevanceEntityToEntityMappingRepository.saveAndFlush(newMap);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(newMap.getId()) //
                    .withCommandId(command.commandId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    @Transactional
    public CommandProcessingResult updateEntityToEntityMapping(Long mapId, JsonCommand command) {
        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final CorevanceEntityToEntityMapping mapForUpdate = this.corevanceEntityToEntityMappingRepositoryWrapper
                    .findOneWithNotFoundDetection(mapId);

            String relId = mapForUpdate.getRelationId().getId().toString();
            final Long fromId = command.longValueOfParameterNamed(CorevanceEntityApiResourceConstants.fromEnityType);
            final Long toId = command.longValueOfParameterNamed(CorevanceEntityApiResourceConstants.toEntityType);
            fromApiJsonDeserializer.checkForEntity(relId, fromId, toId);

            final Map<String, Object> changes = mapForUpdate.updateMap(command);

            if (!changes.isEmpty()) {
                this.corevanceEntityToEntityMappingRepository.saveAndFlush(mapForUpdate);
            }
            return new CommandProcessingResultBuilder() //
                    .withEntityId(mapForUpdate.getId()) //
                    .withCommandId(command.commandId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteEntityToEntityMapping(Long mapId) {
        // TODO Auto-generated method stub

        final CorevanceEntityToEntityMapping deleteMap = this.corevanceEntityToEntityMappingRepositoryWrapper
                .findOneWithNotFoundDetection(mapId);
        this.corevanceEntityToEntityMappingRepository.delete(deleteMap);

        return new CommandProcessingResultBuilder() //
                .withEntityId(deleteMap.getId()) //
                .build();

    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        LOG.error("Problem occurred in handleDataIntegrityIssues function", realCause);
        if (realCause.getMessage().contains("rel_id_from_id_to_id")) {
            final String fromId = command.stringValueOfParameterNamed(CorevanceEntityApiResourceConstants.fromEnityType);
            final String toId = command.stringValueOfParameterNamed(CorevanceEntityApiResourceConstants.toEntityType);
            throw new PlatformDataIntegrityException("error.msg.duplicate.entity.mapping",
                    "EntityMapping from " + fromId + " to " + toId + " already exist");
        }

        LOG.error("Error occured.", dve);
        throw ErrorHandler.getMappable(dve, "error.msg.entity.mapping", "Unknown data integrity issue with resource.");
    }

    /*
     * @Override public CommandProcessingResult updateEntityAccess(Long entityAccessId, JsonCommand command) { // TODO
     * Auto-generated method stub return null; }
     *
     * @Override public CommandProcessingResult removeEntityAccess(String entityType, Long entityId, Long accessType,
     * String secondEntityType, Long secondEntityId) { // TODO Auto-generated method stub return null; }
     */
}
