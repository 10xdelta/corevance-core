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
package com.corevance.portfolio.savings.service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.infrastructure.core.service.Page;
import com.corevance.infrastructure.core.service.SearchParameters;
import com.corevance.portfolio.savings.DepositAccountType;
import com.corevance.portfolio.savings.data.SavingsAccountData;
import com.corevance.portfolio.savings.data.SavingsAccountTransactionData;
import com.corevance.portfolio.savings.data.SavingsAccrualData;
import com.corevance.portfolio.savings.domain.SavingsAccount;

public interface SavingsAccountReadPlatformService {

    Page<SavingsAccountData> retrieveAll(SearchParameters searchParameters);

    Collection<SavingsAccountData> retrieveAllForLookup(Long clientId);

    Collection<SavingsAccountData> retrieveActiveForLookup(Long clientId, DepositAccountType depositAccountType);

    Collection<SavingsAccountData> retrieveActiveForLookup(Long clientId, DepositAccountType depositAccountType, String currencyCode);

    SavingsAccountData retrieveOne(Long savingsId);

    SavingsAccountTransactionData retrieveDepositTransactionTemplate(Long savingsId, DepositAccountType depositAccountType);

    Collection<SavingsAccountTransactionData> retrieveAllTransactions(Long savingsId, DepositAccountType depositAccountType);

    // Collection<SavingsAccountAnnualFeeData>
    // retrieveAccountsWithAnnualFeeDue();

    SavingsAccountTransactionData retrieveSavingsTransaction(Long savingsId, Long transactionId, DepositAccountType depositAccountType);

    Collection<SavingsAccountData> retrieveForLookup(Long clientId, Boolean overdraft);

    List<Long> retrieveSavingsIdsPendingInactive(LocalDate tenantLocalDate);

    List<Long> retrieveSavingsIdsPendingDormant(LocalDate tenantLocalDate);

    List<Long> retrieveSavingsIdsPendingEscheat(LocalDate tenantLocalDate);

    boolean isAccountBelongsToClient(Long clientId, Long accountId, DepositAccountType depositAccountType, String currencyCode);

    String retrieveAccountNumberByAccountId(Long accountId);

    List<Long> getAccountsIdsByStatusPaged(Integer status, int pageSize, Long maxSavingsIdInList);

    List<SavingsAccountData> retrieveAllSavingsDataForInterestPosting(boolean backdatedTxnsAllowedTill, int pageSize, Integer status,
            Long maxSavingsId);

    List<SavingsAccountTransactionData> retrieveAllTransactionData(List<String> refNo);

    Long retrieveSavingsTransactionIdByExternalId(ExternalId externalId);

    Long retrieveAccountIdByExternalId(ExternalId externalId);

    List<SavingsAccrualData> retrievePeriodicAccrualData(LocalDate tillDate, SavingsAccount savings);
}
