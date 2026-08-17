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
package com.corevance.interoperation.service;

import java.time.LocalDateTime;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.interoperation.data.InteropAccountData;
import com.corevance.interoperation.data.InteropIdentifierAccountResponseData;
import com.corevance.interoperation.data.InteropIdentifiersResponseData;
import com.corevance.interoperation.data.InteropKycResponseData;
import com.corevance.interoperation.data.InteropQuoteResponseData;
import com.corevance.interoperation.data.InteropTransactionRequestResponseData;
import com.corevance.interoperation.data.InteropTransactionsData;
import com.corevance.interoperation.data.InteropTransferResponseData;
import com.corevance.interoperation.domain.InteropIdentifierType;
import org.springframework.lang.NonNull;

public interface InteropService {

    @NonNull
    InteropIdentifiersResponseData getAccountIdentifiers(@NonNull String accountId);

    @NonNull
    InteropAccountData getAccountDetails(@NonNull String accountId);

    @NonNull
    InteropTransactionsData getAccountTransactions(@NonNull String accountId, boolean debit, boolean credit, LocalDateTime transactionsFrom,
            LocalDateTime transactionsTo);

    @NonNull
    InteropIdentifierAccountResponseData getAccountByIdentifier(@NonNull InteropIdentifierType idType, @NonNull String idValue,
            String subIdOrType);

    @NonNull
    InteropIdentifierAccountResponseData registerAccountIdentifier(@NonNull InteropIdentifierType idType, @NonNull String idValue,
            String subIdOrType, @NonNull JsonCommand command);

    @NonNull
    InteropIdentifierAccountResponseData deleteAccountIdentifier(@NonNull InteropIdentifierType idType, @NonNull String idValue,
            String subIdOrType);

    InteropTransactionRequestResponseData getTransactionRequest(@NonNull String transactionCode, @NonNull String requestCode);

    @NonNull
    InteropTransactionRequestResponseData createTransactionRequest(@NonNull JsonCommand command);

    InteropQuoteResponseData getQuote(@NonNull String transactionCode, @NonNull String quoteCode);

    @NonNull
    InteropQuoteResponseData createQuote(@NonNull JsonCommand command);

    InteropTransferResponseData getTransfer(@NonNull String transactionCode, @NonNull String transferCode);

    @NonNull
    InteropTransferResponseData prepareTransfer(@NonNull JsonCommand command);

    @NonNull
    InteropTransferResponseData commitTransfer(@NonNull JsonCommand command);

    @NonNull
    InteropTransferResponseData releaseTransfer(@NonNull JsonCommand command);

    @NonNull
    InteropKycResponseData getKyc(@NonNull String accountId);

    @NonNull
    String disburseLoan(@NonNull String accountId, String apiRequestBodyAsJson);

    @NonNull
    String loanRepayment(@NonNull String accountId, String apiRequestBodyAsJson);
}
