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
package com.corevance.infrastructure.event.external.service.serialization.serializer.share;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import com.corevance.avro.generator.ByteBufferSerializable;
import com.corevance.avro.share.v1.ShareProductDataV1;
import com.corevance.infrastructure.event.business.domain.BusinessEvent;
import com.corevance.infrastructure.event.business.domain.share.ShareProductDividentsCreateBusinessEvent;
import com.corevance.infrastructure.event.external.service.serialization.mapper.share.ShareProductDataMapper;
import com.corevance.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import com.corevance.portfolio.products.service.ShareProductReadPlatformService;
import com.corevance.portfolio.shareproducts.data.ShareProductData;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShareProductDividentsCreateBusinessEventSerializer implements BusinessEventSerializer {

    private final ShareProductReadPlatformService service;
    private final ShareProductDataMapper mapper;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof ShareProductDividentsCreateBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        ShareProductDividentsCreateBusinessEvent event = (ShareProductDividentsCreateBusinessEvent) rawEvent;
        ShareProductData data = (ShareProductData) service.retrieveOne(event.get(), false);
        return mapper.map(data);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return ShareProductDataV1.class;
    }
}
