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

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.service.MathUtil;
import com.corevance.investor.data.ExternalTransferStatus;
import com.corevance.investor.data.ExternalTransferSubStatus;
import com.corevance.investor.domain.ExternalAssetOwnerTransfer;
import com.corevance.portfolio.loanaccount.domain.Loan;

@RequiredArgsConstructor
public class LoanTransferabilityServiceImpl implements LoanTransferabilityService {

    private final DelayedSettlementAttributeService delayedSettlementAttributeService;

    @Override
    public boolean isTransferable(final Loan loan, final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        if (shouldValidateTransferable(loan, externalAssetOwnerTransfer)) {
            return MathUtil.nullToDefault(loan.getSummary().getTotalOutstanding(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0;
        }

        return true;
    }

    @Override
    public ExternalTransferSubStatus getDeclinedSubStatus(final Loan loan) {
        if (MathUtil.nullToDefault(loan.getTotalOverpaid(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0) {
            return ExternalTransferSubStatus.BALANCE_NEGATIVE;
        }

        return ExternalTransferSubStatus.BALANCE_ZERO;
    }

    private boolean shouldValidateTransferable(final Loan loan, final ExternalAssetOwnerTransfer externalAssetOwnerTransfer) {
        if (!delayedSettlementAttributeService.isEnabled(loan.getLoanProduct().getId())) {
            // When delayed settlement is disabled, asset is directly sold to investor. Need to validate.
            return true;
        }

        // When delayed settlement is enabled and asset is sold to intermediate. Need to validate.
        return ExternalTransferStatus.PENDING_INTERMEDIATE == externalAssetOwnerTransfer.getStatus();

        // When delayed settlement is enabled and asset is sold from intermediate to investor. No need to validate.
    }
}
