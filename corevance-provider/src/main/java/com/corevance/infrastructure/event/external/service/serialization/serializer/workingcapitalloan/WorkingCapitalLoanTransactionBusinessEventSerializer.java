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
package com.corevance.infrastructure.event.external.service.serialization.serializer.workingcapitalloan;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import com.corevance.avro.generator.ByteBufferSerializable;
import com.corevance.avro.workingcapitalloan.v1.WorkingCapitalLoanTransactionDataV1;
import com.corevance.infrastructure.event.business.domain.BusinessEvent;
import com.corevance.infrastructure.event.business.domain.workingcapitalloan.transaction.WorkingCapitalLoanTransactionBusinessEvent;
import com.corevance.infrastructure.event.external.service.serialization.mapper.workingcapitalloan.WorkingCapitalLoanTransactionDataMapper;
import com.corevance.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import com.corevance.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import com.corevance.portfolio.workingcapitalloan.service.WorkingCapitalLoanTransactionReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanTransactionBusinessEventSerializer implements BusinessEventSerializer {

    private final WorkingCapitalLoanTransactionReadPlatformService readPlatformService;
    private final WorkingCapitalLoanTransactionDataMapper mapper;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof WorkingCapitalLoanTransactionBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        final WorkingCapitalLoanTransactionBusinessEvent event = (WorkingCapitalLoanTransactionBusinessEvent) rawEvent;
        final Long wcLoanId = event.get().getWcLoan().getId();
        final Long wcLoanTransactionId = event.get().getId();
        final WorkingCapitalLoanTransactionData data = readPlatformService.retrieveTransaction(wcLoanId, wcLoanTransactionId);
        return mapper.map(data);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return WorkingCapitalLoanTransactionDataV1.class;
    }
}
