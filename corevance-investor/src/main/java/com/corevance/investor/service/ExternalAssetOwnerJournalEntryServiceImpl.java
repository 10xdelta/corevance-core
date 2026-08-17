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
package com.corevance.investor.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.accounting.journalentry.domain.JournalEntry;
import com.corevance.infrastructure.event.business.domain.journalentry.LoanJournalEntryCreatedBusinessEvent;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.investor.config.InvestorModuleIsEnabledCondition;
import com.corevance.investor.domain.ExternalAssetOwnerJournalEntryMapping;
import com.corevance.investor.domain.ExternalAssetOwnerJournalEntryMappingRepository;
import com.corevance.investor.domain.ExternalAssetOwnerTransferLoanMappingRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Conditional(InvestorModuleIsEnabledCondition.class)
public class ExternalAssetOwnerJournalEntryServiceImpl implements ExternalAssetOwnerJournalEntryService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final ExternalAssetOwnerJournalEntryMappingRepository externalAssetOwnerJournalEntryMappingRepository;
    private final ExternalAssetOwnerTransferLoanMappingRepository externalAssetOwnerTransferLoanMappingRepository;
    private final LoanTransactionRepository loanTransactionRepository;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(LoanJournalEntryCreatedBusinessEvent.class, event -> {
            JournalEntry journalEntry = event.get();

            Long loanId = loanTransactionRepository.findLoanIdById(journalEntry.getLoanTransactionId()).orElseThrow();

            externalAssetOwnerTransferLoanMappingRepository.findByLoanId(loanId).ifPresent(transferLoanMapping -> {
                ExternalAssetOwnerJournalEntryMapping mapping = new ExternalAssetOwnerJournalEntryMapping();
                mapping.setJournalEntry(journalEntry);
                mapping.setOwner(transferLoanMapping.getOwnerTransfer().getOwner());
                externalAssetOwnerJournalEntryMappingRepository.saveAndFlush(mapping);
            });
        });
    }
}
