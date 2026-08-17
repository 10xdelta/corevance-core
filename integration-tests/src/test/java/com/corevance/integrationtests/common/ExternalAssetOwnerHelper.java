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
package com.corevance.integrationtests.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.corevance.accounting.common.AccountingConstants;
import com.corevance.client.models.ExternalAssetOwnerRequest;
import com.corevance.client.models.ExternalAssetOwnerSearchRequest;
import com.corevance.client.models.ExternalOwnerJournalEntryData;
import com.corevance.client.models.ExternalOwnerTransferJournalEntryData;
import com.corevance.client.models.ExternalTransferData;
import com.corevance.client.models.GetFinancialActivityAccountsResponse;
import com.corevance.client.models.PageExternalTransferData;
import com.corevance.client.models.PagedRequestExternalAssetOwnerSearchRequest;
import com.corevance.client.models.PostFinancialActivityAccountsRequest;
import com.corevance.client.models.PostInitiateTransferResponse;
import com.corevance.client.util.CallFailedRuntimeException;
import com.corevance.client.util.Calls;
import com.corevance.integrationtests.common.accounting.Account;
import com.corevance.integrationtests.common.accounting.FinancialActivityAccountHelper;

public class ExternalAssetOwnerHelper {

    public ExternalAssetOwnerHelper() {}

    public PostInitiateTransferResponse initiateTransferByLoanId(Long loanId, String command, ExternalAssetOwnerRequest request) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.transferRequestWithLoanId(loanId, request, command));
    }

    public void cancelTransferByTransferExternalId(String transferExternalId) {
        Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.transferRequestWithIdByExternalId(transferExternalId,
                "cancel"));
    }

    public void cancelTransferByTransferExternalIdError(String transferExternalId) {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> Calls.okR(CorevanceClientHelper.getCorevanceClient().externalAssetOwners
                        .transferRequestWithIdByExternalId(transferExternalId, "cancel")));
        assertEquals(403, exception.getResponse().code());
    }

    public PageExternalTransferData retrieveTransferByTransferExternalId(String transferExternalId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getTransfers(transferExternalId, null, null, 0, 100));
    }

    public PageExternalTransferData retrieveTransferByLoanExternalId(String loanExternalId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getTransfers(null, null, loanExternalId, 0, 100));
    }

    public PageExternalTransferData retrieveTransfersByLoanId(Long loanId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getTransfers(null, loanId, null, 0, 100));
    }

    public PageExternalTransferData retrieveTransfersByLoanId(Long loanId, int offset, int limit) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getTransfers(null, loanId, null, offset, limit));
    }

    public ExternalTransferData retrieveActiveTransferByLoanExternalId(String loanExternalId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getActiveTransfer(null, null, loanExternalId));
    }

    public ExternalTransferData retrieveActiveTransferByTransferExternalId(String transferExternalId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getActiveTransfer(transferExternalId, null, null));
    }

    public ExternalTransferData retrieveActiveTransferByLoanId(Long loanId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getActiveTransfer(null, loanId, null));
    }

    public ExternalOwnerTransferJournalEntryData retrieveJournalEntriesOfTransfer(Long transferId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getJournalEntriesOfTransfer(transferId, 0, 100));
    }

    public ExternalOwnerJournalEntryData retrieveJournalEntriesOfOwner(String ownerExternalId) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.getJournalEntriesOfOwner(ownerExternalId, 0, 100));
    }

    public PageExternalTransferData searchExternalAssetOwnerTransfer(PagedRequestExternalAssetOwnerSearchRequest request) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().externalAssetOwners.searchInvestorData(request));
    }

    public PagedRequestExternalAssetOwnerSearchRequest buildExternalAssetOwnerSearchRequest(String text, String attribute,
            LocalDate fromDate, LocalDate toDate, Integer page, Integer size) {
        // increase it if tests create more than 200 items
        final Integer DEFAULT_PAGE_SIZE = 200;
        PagedRequestExternalAssetOwnerSearchRequest pagedRequest = new PagedRequestExternalAssetOwnerSearchRequest();
        ExternalAssetOwnerSearchRequest searchRequest = new ExternalAssetOwnerSearchRequest();
        searchRequest.text(text);
        if (attribute.equals("effective")) {
            searchRequest.setEffectiveFromDate(fromDate);
            searchRequest.setEffectiveToDate(toDate);
        } else if (attribute.equals("settlement")) {
            searchRequest.setSubmittedFromDate(fromDate);
            searchRequest.setSubmittedToDate(toDate);
        }
        pagedRequest.setRequest(searchRequest);
        pagedRequest.setSorts(new ArrayList<>());
        pagedRequest.setPage(page != null ? page : 0);
        pagedRequest.setSize(size != null ? size : DEFAULT_PAGE_SIZE);
        return pagedRequest;
    }

    public void setProperFinancialActivity(FinancialActivityAccountHelper financialActivityAccountHelper, Account transferAccount) {
        List<GetFinancialActivityAccountsResponse> financialMappings = financialActivityAccountHelper.getAllFinancialActivityAccounts();
        financialMappings.forEach(mapping -> financialActivityAccountHelper.deleteFinancialActivityAccount(mapping.getId()));
        financialActivityAccountHelper.createFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                .financialActivityId((long) AccountingConstants.FinancialActivity.ASSET_TRANSFER.getValue())
                .glAccountId((long) transferAccount.getAccountID()));
    }

}
