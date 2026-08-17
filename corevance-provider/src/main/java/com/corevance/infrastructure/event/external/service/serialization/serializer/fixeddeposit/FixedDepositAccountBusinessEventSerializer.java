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
package com.corevance.infrastructure.event.external.service.serialization.serializer.fixeddeposit;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import com.corevance.avro.fixeddeposit.v1.FixedDepositAccountDataV1;
import com.corevance.avro.generator.ByteBufferSerializable;
import com.corevance.infrastructure.event.business.domain.BusinessEvent;
import com.corevance.infrastructure.event.business.domain.deposit.FixedDepositAccountBusinessEvent;
import com.corevance.infrastructure.event.external.service.serialization.mapper.fixeddeposit.FixedDepositAccountDataMapper;
import com.corevance.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import com.corevance.portfolio.savings.DepositAccountType;
import com.corevance.portfolio.savings.data.FixedDepositAccountData;
import com.corevance.portfolio.savings.service.DepositAccountReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FixedDepositAccountBusinessEventSerializer implements BusinessEventSerializer {

    private final DepositAccountReadPlatformService service;
    private final FixedDepositAccountDataMapper mapper;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof FixedDepositAccountBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        FixedDepositAccountBusinessEvent event = (FixedDepositAccountBusinessEvent) rawEvent;
        FixedDepositAccountData data = (FixedDepositAccountData) service.retrieveOne(DepositAccountType.FIXED_DEPOSIT, event.get().getId());
        return mapper.map(data);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return FixedDepositAccountDataV1.class;
    }
}
