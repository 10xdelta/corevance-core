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
package com.corevance.infrastructure.event.external.service.serialization.serializer.loan;

import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import com.corevance.avro.generator.ByteBufferSerializable;
import com.corevance.avro.loan.v1.LoanChargeDeletedV1;
import com.corevance.infrastructure.event.business.domain.BusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.charge.LoanDeleteChargeBusinessEvent;
import com.corevance.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import com.corevance.infrastructure.event.external.service.support.ByteBufferConverter;
import com.corevance.portfolio.loanaccount.domain.LoanCharge;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class LoanChargeDeletedBusinessEventSerializer implements BusinessEventSerializer {

    private final ByteBufferConverter byteBufferConverter;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanDeleteChargeBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        LoanDeleteChargeBusinessEvent event = (LoanDeleteChargeBusinessEvent) rawEvent;
        LoanCharge loanCharge = event.get();
        Long id = loanCharge.getId();
        Long chargeId = loanCharge.getCharge().getId();
        return new LoanChargeDeletedV1(id, chargeId);
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanChargeDeletedV1.class;
    }
}
