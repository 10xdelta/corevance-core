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
package com.corevance.infrastructure.entityaccess.domain;

import com.corevance.infrastructure.entityaccess.exception.CorevanceEntityAccessNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CorevanceEntityToEntityMappingRepositoryWrapper {

    private final CorevanceEntityToEntityMappingRepository corevanceEntityToEntityMappingRepository;

    @Autowired
    public CorevanceEntityToEntityMappingRepositoryWrapper(
            final CorevanceEntityToEntityMappingRepository corevanceEntityToEntityMappingRepository) {
        this.corevanceEntityToEntityMappingRepository = corevanceEntityToEntityMappingRepository;
    }

    public CorevanceEntityToEntityMapping findOneWithNotFoundDetection(final Long id) {
        return this.corevanceEntityToEntityMappingRepository.findById(id).orElseThrow(() -> new CorevanceEntityAccessNotFoundException(id));
    }

    public void delete(final CorevanceEntityToEntityMapping mapId) {
        this.corevanceEntityToEntityMappingRepository.delete(mapId);
    }

}
