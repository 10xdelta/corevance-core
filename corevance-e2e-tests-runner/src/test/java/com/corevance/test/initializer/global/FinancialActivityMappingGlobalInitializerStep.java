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
package com.corevance.test.initializer.global;

import static com.corevance.client.feign.util.FeignCalls.executeVoid;
import static com.corevance.test.data.accounttype.DefaultAccountType.AA_SUSPENSE_BALANCE;
import static com.corevance.test.data.accounttype.DefaultAccountType.ASSET_TRANSFER;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.feign.util.CallFailedRuntimeException;
import com.corevance.client.models.PostFinancialActivityAccountsRequest;
import com.corevance.test.data.accounttype.AccountTypeResolver;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FinancialActivityMappingGlobalInitializerStep implements CorevanceGlobalInitializerStep {

    public static final Long FINANCIAL_ACTIVITY_ID_ASSET_TRANSFER = 100L;
    public static final Long FINANCIAL_ACTIVITY_ID_LIABILITY_TRANSFER = 200L;

    private final CorevanceFeignClient corevanceClient;
    private final AccountTypeResolver accountTypeResolver;

    @Override
    public void initialize() {
        Long assetTransferGlAccountId = accountTypeResolver.resolve(ASSET_TRANSFER);
        PostFinancialActivityAccountsRequest assetTransferRequest = new PostFinancialActivityAccountsRequest()
                .financialActivityId(FINANCIAL_ACTIVITY_ID_ASSET_TRANSFER).glAccountId(assetTransferGlAccountId);

        Long liabilityTransferGlAccountId = accountTypeResolver.resolve(AA_SUSPENSE_BALANCE);
        PostFinancialActivityAccountsRequest requestLiabilityTransfer = new PostFinancialActivityAccountsRequest()
                .financialActivityId(FINANCIAL_ACTIVITY_ID_LIABILITY_TRANSFER).glAccountId(liabilityTransferGlAccountId);

        try {
            executeVoid(() -> corevanceClient.mappingFinancialActivitiesToAccounts()
                    .createGLAccountMappingFinancialActivityAccount(assetTransferRequest, Map.of()));

            executeVoid(() -> corevanceClient.mappingFinancialActivitiesToAccounts()
                    .createGLAccountMappingFinancialActivityAccount(requestLiabilityTransfer, Map.of()));
            log.debug("Financial activity mapping created successfully");
        } catch (CallFailedRuntimeException e) {
            if (e.getStatus() == 403 && e.getDeveloperMessage() != null && e.getDeveloperMessage().contains("already exists")) {
                log.debug("Financial activity mapping already exists, skipping creation");
                return;
            }
            throw e;
        }
    }
}
