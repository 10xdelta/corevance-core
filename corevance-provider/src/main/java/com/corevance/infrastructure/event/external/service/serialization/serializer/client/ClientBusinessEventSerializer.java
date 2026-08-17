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
package com.corevance.infrastructure.event.external.service.serialization.serializer.client;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import com.corevance.avro.client.v1.ClientDataV1;
import com.corevance.avro.generator.ByteBufferSerializable;
import com.corevance.infrastructure.event.business.domain.BusinessEvent;
import com.corevance.infrastructure.event.business.domain.client.ClientBusinessEvent;
import com.corevance.infrastructure.event.external.service.serialization.mapper.client.ClientDataMapper;
import com.corevance.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import com.corevance.portfolio.client.data.ClientData;
import com.corevance.portfolio.client.service.ClientReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientBusinessEventSerializer implements BusinessEventSerializer {

    private final ClientReadPlatformService service;
    private final ClientDataMapper mapper;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof ClientBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        ClientBusinessEvent event = (ClientBusinessEvent) rawEvent;
        ClientData data = service.retrieveOne(event.get().getId());
        return mapper.map(data);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return ClientDataV1.class;
    }
}
