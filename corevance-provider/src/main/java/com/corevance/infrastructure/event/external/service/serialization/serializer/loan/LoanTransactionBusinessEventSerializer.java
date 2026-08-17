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

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import com.corevance.avro.generator.ByteBufferSerializable;
import com.corevance.avro.loan.v1.LoanTransactionDataV1;
import com.corevance.avro.loan.v1.LoanTransactionFlagsDataV1;
import com.corevance.infrastructure.event.business.domain.BusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.transaction.LoanTransactionFlagsData;
import com.corevance.infrastructure.event.external.service.serialization.mapper.loan.LoanTransactionDataMapper;
import com.corevance.infrastructure.event.external.service.serialization.serializer.AbstractBusinessEventWithCustomDataSerializer;
import com.corevance.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import com.corevance.infrastructure.event.external.service.serialization.serializer.ExternalEventCustomDataSerializer;
import com.corevance.portfolio.loanaccount.data.LoanTransactionData;
import com.corevance.portfolio.loanaccount.service.LoanChargePaidByReadService;
import com.corevance.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanTransactionBusinessEventSerializer extends AbstractBusinessEventWithCustomDataSerializer<LoanTransactionBusinessEvent>
        implements BusinessEventSerializer {

    private final LoanReadPlatformService service;
    private final LoanTransactionDataMapper loanTransactionMapper;
    private final LoanChargePaidByReadService loanChargePaidByReadService;
    private final List<ExternalEventCustomDataSerializer<LoanTransactionBusinessEvent>> externalEventCustomDataSerializers;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanTransactionBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        LoanTransactionBusinessEvent event = (LoanTransactionBusinessEvent) rawEvent;
        Long loanId = event.get().getLoan().getId();
        Long loanTransactionId = event.get().getId();
        LoanTransactionData transactionData = service.retrieveLoanTransaction(loanId, loanTransactionId);
        transactionData.setLoanChargePaidByList(loanChargePaidByReadService.fetchLoanChargesPaidByDataTransactionId(loanTransactionId));

        final LoanTransactionDataV1 result = loanTransactionMapper.map(transactionData);
        result.setCustomData(collectCustomData(event));

        LoanTransactionFlagsData flags = event.getFlags();
        if (flags != null) {
            result.setFlags(LoanTransactionFlagsDataV1.newBuilder().setChangedTerms(flags.changedTerms()).build());
        }

        return result;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanTransactionDataV1.class;
    }

    @Override
    protected List<ExternalEventCustomDataSerializer<LoanTransactionBusinessEvent>> getExternalEventCustomDataSerializers() {
        return externalEventCustomDataSerializers;
    }
}
