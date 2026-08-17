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
package com.corevance.infrastructure.event.external.service.serialization.serializer.savings;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import com.corevance.avro.generator.ByteBufferSerializable;
import com.corevance.avro.savings.v1.SavingsAccountTransactionDataV1;
import com.corevance.infrastructure.event.business.domain.BusinessEvent;
import com.corevance.infrastructure.event.business.domain.savings.transaction.SavingsAccountTransactionBusinessEvent;
import com.corevance.infrastructure.event.external.service.serialization.mapper.savings.SavingsAccountTransactionDataMapper;
import com.corevance.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import com.corevance.portfolio.savings.data.SavingsAccountTransactionData;
import com.corevance.portfolio.savings.domain.SavingsAccountTransaction;
import com.corevance.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SavingsAccountTransactionBusinessEventSerializer implements BusinessEventSerializer {

    private final SavingsAccountReadPlatformService service;
    private final SavingsAccountTransactionDataMapper mapper;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof SavingsAccountTransactionBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        SavingsAccountTransactionBusinessEvent event = (SavingsAccountTransactionBusinessEvent) rawEvent;
        SavingsAccountTransaction tx = event.get();
        SavingsAccountTransactionData data = service.retrieveSavingsTransaction(tx.getSavingsAccount().getId(), tx.getId(),
                tx.getSavingsAccount().depositAccountType());
        return mapper.map(data);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return SavingsAccountTransactionDataV1.class;
    }
}
