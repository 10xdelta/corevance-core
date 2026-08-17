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
package com.corevance.accounting.provisioning.service;

import com.google.gson.JsonObject;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.accounting.glaccount.domain.GLAccount;
import com.corevance.accounting.glaccount.domain.GLAccountRepository;
import com.corevance.accounting.glaccount.exception.GLAccountNotFoundException;
import com.corevance.accounting.journalentry.service.JournalEntryWritePlatformService;
import com.corevance.accounting.provisioning.data.LoanProductProvisioningEntryData;
import com.corevance.accounting.provisioning.data.ProvisioningEntryData;
import com.corevance.accounting.provisioning.domain.LoanProductProvisioningEntry;
import com.corevance.accounting.provisioning.domain.ProvisioningEntry;
import com.corevance.accounting.provisioning.domain.ProvisioningEntryRepository;
import com.corevance.accounting.provisioning.exception.NoProvisioningCriteriaDefinitionFound;
import com.corevance.accounting.provisioning.exception.ProvisioningEntryAlreadyCreatedException;
import com.corevance.accounting.provisioning.exception.ProvisioningEntryNotfoundException;
import com.corevance.accounting.provisioning.exception.ProvisioningJournalEntriesCannotbeCreatedException;
import com.corevance.accounting.provisioning.serialization.ProvisioningEntriesDefinitionJsonDeserializer;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.data.CommandProcessingResultBuilder;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.organisation.monetary.domain.MonetaryCurrency;
import com.corevance.organisation.monetary.domain.Money;
import com.corevance.organisation.monetary.domain.MoneyHelper;
import com.corevance.organisation.office.domain.Office;
import com.corevance.organisation.office.domain.OfficeRepository;
import com.corevance.organisation.office.exception.OfficeNotFoundException;
import com.corevance.organisation.provisioning.data.ProvisioningCriteriaData;
import com.corevance.organisation.provisioning.domain.ProvisioningCategory;
import com.corevance.organisation.provisioning.domain.ProvisioningCategoryRepository;
import com.corevance.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import com.corevance.portfolio.PortfolioProductType;
import com.corevance.portfolio.loanproduct.domain.LoanProduct;
import com.corevance.portfolio.loanproduct.domain.LoanProductRepository;
import com.corevance.portfolio.loanproduct.exception.LoanProductNotFoundException;
import com.corevance.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;

@RequiredArgsConstructor
@Slf4j
public class ProvisioningEntriesWritePlatformServiceJpaRepositoryImpl implements ProvisioningEntriesWritePlatformService {

    private final ProvisioningEntriesReadPlatformService provisioningEntriesReadPlatformService;
    private final ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService;
    private final LoanProductRepository loanProductRepository;
    private final GLAccountRepository glAccountRepository;
    private final OfficeRepository officeRepository;
    private final ProvisioningCategoryRepository provisioningCategoryRepository;
    private final PlatformSecurityContext platformSecurityContext;
    private final ProvisioningEntryRepository provisioningEntryRepository;
    private final JournalEntryWritePlatformService journalEntryWritePlatformService;
    private final ProvisioningEntriesDefinitionJsonDeserializer fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;

    @Override
    public CommandProcessingResult createProvisioningJournalEntries(Long provisioningEntryId, JsonCommand command) {
        ProvisioningEntry requestedEntry = this.provisioningEntryRepository.findById(provisioningEntryId)
                .orElseThrow(() -> new ProvisioningEntryNotfoundException(provisioningEntryId));

        ProvisioningEntryData exisProvisioningEntryData = this.provisioningEntriesReadPlatformService
                .retrieveExistingProvisioningIdDateWithJournals();
        revertAndAddJournalEntries(exisProvisioningEntryData, requestedEntry);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(requestedEntry.getId()) //
                .build();
    }

    private void revertAndAddJournalEntries(ProvisioningEntryData existingEntryData, ProvisioningEntry requestedEntry) {
        if (existingEntryData != null) {
            validateForCreateJournalEntry(existingEntryData, requestedEntry);
            this.journalEntryWritePlatformService.revertProvisioningJournalEntries(requestedEntry.getCreatedDate(),
                    existingEntryData.getId(), PortfolioProductType.PROVISIONING.getValue());
        }
        if (requestedEntry.getLoanProductProvisioningEntries() == null || requestedEntry.getLoanProductProvisioningEntries().size() == 0) {
            requestedEntry.setIsJournalEntryCreated(Boolean.FALSE);
        } else {
            requestedEntry.setIsJournalEntryCreated(Boolean.TRUE);
        }

        this.provisioningEntryRepository.saveAndFlush(requestedEntry);
        this.journalEntryWritePlatformService.createProvisioningJournalEntries(requestedEntry);
    }

    private void validateForCreateJournalEntry(ProvisioningEntryData existingEntry, ProvisioningEntry requested) {
        LocalDate existingDate = existingEntry.getCreatedDate();
        LocalDate requestedDate = requested.getCreatedDate();
        if (!DateUtils.isBefore(existingDate, requestedDate)) {
            throw new ProvisioningJournalEntriesCannotbeCreatedException(existingEntry.getCreatedDate(), requestedDate);
        }
    }

    private boolean isJournalEntriesRequired(JsonCommand command) {
        boolean bool = false;
        if (this.fromApiJsonHelper.parameterExists("createjournalentries", command.parsedJson())) {
            JsonObject jsonObject = command.parsedJson().getAsJsonObject();
            bool = jsonObject.get("createjournalentries").getAsBoolean();
        }
        return bool;
    }

    private LocalDate parseDate(JsonCommand command) {
        return this.fromApiJsonHelper.extractLocalDateNamed("date", command.parsedJson());
    }

    @Override
    public CommandProcessingResult createProvisioningEntries(JsonCommand command) {
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        LocalDate createdDate = parseDate(command);
        boolean addJournalEntries = isJournalEntriesRequired(command);
        try {
            Collection<ProvisioningCriteriaData> criteriaCollection = this.provisioningCriteriaReadPlatformService
                    .retrieveAllProvisioningCriterias();
            if (criteriaCollection == null || criteriaCollection.isEmpty()) {
                throw new NoProvisioningCriteriaDefinitionFound();
            }
            ProvisioningEntry requestedEntry = createProvisioningEntry(createdDate, addJournalEntries);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(requestedEntry.getId()) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public ProvisioningEntry createProvisioningEntry(LocalDate date, boolean addJournalEntries) {
        ProvisioningEntry existingEntry = this.provisioningEntryRepository.findByProvisioningEntryDate(date);
        if (existingEntry != null) {
            throw new ProvisioningEntryAlreadyCreatedException(existingEntry.getId(), existingEntry.getCreatedDate());
        }
        AppUser currentUser = this.platformSecurityContext.authenticatedUser();
        ProvisioningEntry requestedEntry = new ProvisioningEntry().setCreatedBy(currentUser).setCreatedDate(date);
        Collection<LoanProductProvisioningEntry> entries = generateLoanProvisioningEntry(requestedEntry, date);
        requestedEntry.setProvisioningEntries(entries);
        if (addJournalEntries) {
            ProvisioningEntryData existingProvisioningEntryData = this.provisioningEntriesReadPlatformService
                    .retrieveExistingProvisioningIdDateWithJournals();
            revertAndAddJournalEntries(existingProvisioningEntryData, requestedEntry);
        } else {
            this.provisioningEntryRepository.saveAndFlush(requestedEntry);
        }
        return requestedEntry;
    }

    @Override
    public CommandProcessingResult reCreateProvisioningEntries(Long provisioningEntryId, JsonCommand command) {
        ProvisioningEntry requestedEntry = this.provisioningEntryRepository.findById(provisioningEntryId)
                .orElseThrow(() -> new ProvisioningEntryNotfoundException(provisioningEntryId));
        requestedEntry.getLoanProductProvisioningEntries().clear();
        this.provisioningEntryRepository.saveAndFlush(requestedEntry);
        Collection<LoanProductProvisioningEntry> entries = generateLoanProvisioningEntry(requestedEntry, requestedEntry.getCreatedDate());
        requestedEntry.setProvisioningEntries(entries);
        this.provisioningEntryRepository.saveAndFlush(requestedEntry);
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(requestedEntry.getId()) //
                .build();
    }

    private Collection<LoanProductProvisioningEntry> generateLoanProvisioningEntry(ProvisioningEntry parent, LocalDate date) {
        Collection<LoanProductProvisioningEntryData> entries = this.provisioningEntriesReadPlatformService
                .retrieveLoanProductsProvisioningData(date);
        // Collect all referenced IDs upfront and bulk-fetch via findAllById,
        // replacing the previous pattern of N x 5 individual repository calls per
        // loop iteration (consistent with the optimisation in COREVANCE-2561).
        Set<Long> productIds = entries.stream().map(LoanProductProvisioningEntryData::getProductId).collect(Collectors.toSet());
        Set<Long> officeIds = entries.stream().map(LoanProductProvisioningEntryData::getOfficeId).collect(Collectors.toSet());
        Set<Long> categoryIds = entries.stream().map(LoanProductProvisioningEntryData::getCategoryId).collect(Collectors.toSet());
        Set<Long> glAccountIds = entries.stream().flatMap(d -> Stream.of(d.getLiablityAccount(), d.getExpenseAccount()))
                .collect(Collectors.toSet());

        Map<Long, LoanProduct> loanProductMap = loanProductRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(LoanProduct::getId, Function.identity()));
        Map<Long, Office> officeMap = officeRepository.findAllById(officeIds).stream()
                .collect(Collectors.toMap(Office::getId, Function.identity()));
        Map<Long, ProvisioningCategory> categoryMap = provisioningCategoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(ProvisioningCategory::getId, Function.identity()));
        Map<Long, GLAccount> glAccountMap = glAccountRepository.findAllById(glAccountIds).stream()
                .collect(Collectors.toMap(GLAccount::getId, Function.identity()));

        Map<Integer, LoanProductProvisioningEntry> provisioningEntries = new HashMap<>();
        for (LoanProductProvisioningEntryData data : entries) {
            LoanProduct loanProduct = loanProductMap.get(data.getProductId());
            if (loanProduct == null) {
                throw new LoanProductNotFoundException(data.getProductId());
            }
            Office office = officeMap.get(data.getOfficeId());
            if (office == null) {
                throw new OfficeNotFoundException(data.getOfficeId());
            }
            GLAccount liabilityAccount = glAccountMap.get(data.getLiablityAccount());
            if (liabilityAccount == null) {
                throw new GLAccountNotFoundException(data.getLiablityAccount());
            }
            GLAccount expenseAccount = glAccountMap.get(data.getExpenseAccount());
            if (expenseAccount == null) {
                throw new GLAccountNotFoundException(data.getExpenseAccount());
            }
            ProvisioningCategory provisioningCategory = categoryMap.get(data.getCategoryId());
            MonetaryCurrency currency = loanProduct.getPrincipalAmount().getCurrency();
            Money money = Money.of(currency, data.getBalance());
            Money amountToReserve = money.percentageOf(data.getPercentage(), MoneyHelper.getMathContext());
            Long criteraId = data.getCriteriaId();
            LoanProductProvisioningEntry entry = new LoanProductProvisioningEntry().setLoanProduct(loanProduct).setOffice(office)
                    .setCurrencyCode(data.getCurrencyCode()).setProvisioningCategory(provisioningCategory)
                    .setOverdueInDays(data.getOverdueInDays()).setReservedAmount(amountToReserve.getAmount())
                    .setLiabilityAccount(liabilityAccount).setExpenseAccount(expenseAccount).setCriteriaId(criteraId);
            entry.setEntry(parent);
            if (!provisioningEntries.containsKey(entry.partialHashCode())) {
                provisioningEntries.put(entry.partialHashCode(), entry);
            } else {
                LoanProductProvisioningEntry entry1 = provisioningEntries.get(entry.partialHashCode());
                entry1.setReservedAmount(entry1.getReservedAmount().add(entry.getReservedAmount()));
            }
        }
        return provisioningEntries.values();
    }
}
