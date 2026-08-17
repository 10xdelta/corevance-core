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

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericContainer;
import org.apache.commons.collections4.CollectionUtils;
import com.corevance.avro.generator.ByteBufferSerializable;
import com.corevance.avro.loan.v1.LoanAccountDataV1;
import com.corevance.avro.loan.v1.LoanInstallmentDelinquencyBucketDataV1;
import com.corevance.infrastructure.event.business.domain.BusinessEvent;
import com.corevance.infrastructure.event.business.domain.loan.LoanBusinessEvent;
import com.corevance.infrastructure.event.external.service.serialization.mapper.loan.LoanAccountDataMapper;
import com.corevance.infrastructure.event.external.service.serialization.serializer.AbstractBusinessEventWithCustomDataSerializer;
import com.corevance.infrastructure.event.external.service.serialization.serializer.BusinessEventSerializer;
import com.corevance.infrastructure.event.external.service.serialization.serializer.ExternalEventCustomDataSerializer;
import com.corevance.portfolio.delinquency.service.DelinquencyReadPlatformService;
import com.corevance.portfolio.loanaccount.api.LoanApiConstants;
import com.corevance.portfolio.loanaccount.data.CollectionData;
import com.corevance.portfolio.loanaccount.data.LoanAccountData;
import com.corevance.portfolio.loanaccount.data.LoanChargeData;
import com.corevance.portfolio.loanaccount.domain.LoanSummaryBalancesRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTermVariations;
import com.corevance.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import com.corevance.portfolio.loanaccount.service.LoanReadPlatformService;
import com.corevance.portfolio.loanaccount.service.LoanSummaryDataProvider;
import com.corevance.portfolio.loanaccount.service.LoanSummaryProviderDelegate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanBusinessEventSerializer extends AbstractBusinessEventWithCustomDataSerializer<LoanBusinessEvent>
        implements BusinessEventSerializer {

    private final LoanReadPlatformService service;
    private final LoanAccountDataMapper mapper;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final LoanInstallmentLevelDelinquencyEventProducer installmentLevelDelinquencyEventProducer;
    private final LoanSummaryBalancesRepository loanSummaryBalancesRepository;
    @Lazy
    private final LoanSummaryProviderDelegate loanSummaryProviderDelegate;
    private final List<ExternalEventCustomDataSerializer<LoanBusinessEvent>> externalEventCustomDataSerializers;

    @Override
    public <T> boolean canSerialize(BusinessEvent<T> event) {
        return event instanceof LoanBusinessEvent;
    }

    @Override
    public <T> ByteBufferSerializable toAvroDTO(BusinessEvent<T> rawEvent) {
        LoanBusinessEvent event = (LoanBusinessEvent) rawEvent;
        Long loanId = event.get().getId();
        LoanAccountData data = service.retrieveOne(loanId);

        data = service.fetchRepaymentScheduleData(data);

        Collection<LoanChargeData> loanCharges = loanChargeReadPlatformService.retrieveLoanCharges(loanId);
        if (CollectionUtils.isNotEmpty(loanCharges)) {
            data.setCharges(loanCharges);
        }

        CollectionData delinquentData = delinquencyReadPlatformService.calculateLoanCollectionData(loanId);
        data.setDelinquent(delinquentData);

        LoanSummaryDataProvider loanSummaryDataProvider = loanSummaryProviderDelegate
                .resolveLoanSummaryDataProvider(data.getTransactionProcessingStrategyCode());

        if (data.getSummary() != null) {
            data.setSummary(loanSummaryDataProvider.withTransactionAmountsSummary(event.get(), data.getSummary(),
                    data.getRepaymentSchedule(), loanSummaryBalancesRepository.retrieveLoanSummaryBalancesByTransactionType(loanId,
                            LoanApiConstants.LOAN_SUMMARY_TRANSACTION_TYPES)));
        } else {
            data.setSummary(loanSummaryDataProvider.withOnlyCurrencyData(data.getCurrency()));
        }

        List<LoanInstallmentDelinquencyBucketDataV1> installmentsDelinquencyData = installmentLevelDelinquencyEventProducer
                .calculateInstallmentLevelDelinquencyData(event.get(), data.getCurrency());

        List<LoanTermVariations> activeLoanTermVariations = event.get().getActiveLoanTermVariations();

        if (!activeLoanTermVariations.isEmpty()) {
            data.setLoanTermVariations(activeLoanTermVariations.stream().map(LoanTermVariations::toData).toList());
        }

        Integer actualNoTerms = Math.toIntExact(
                event.get().getRepaymentScheduleInstallments().stream().filter(i -> !i.isAdditional() && !i.isDownPayment()).count());
        data.setActualNoTerm(actualNoTerms);

        final LoanAccountDataV1 result = mapper.map(data);
        result.getDelinquent().setInstallmentDelinquencyBuckets(installmentsDelinquencyData);

        result.setCustomData(collectCustomData(event));

        return result;
    }

    @Override
    public Class<? extends GenericContainer> getSupportedSchema() {
        return LoanAccountDataV1.class;
    }

    @Override
    protected List<ExternalEventCustomDataSerializer<LoanBusinessEvent>> getExternalEventCustomDataSerializers() {
        return externalEventCustomDataSerializers;
    }
}
