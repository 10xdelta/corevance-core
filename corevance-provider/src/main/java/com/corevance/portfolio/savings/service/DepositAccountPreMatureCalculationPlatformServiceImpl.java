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

import static com.corevance.portfolio.savings.DepositsApiConstants.closedOnDateParamName;

import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.api.JsonQuery;
import com.corevance.infrastructure.core.data.EnumOptionData;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.portfolio.paymenttype.data.PaymentTypeData;
import com.corevance.portfolio.paymenttype.service.PaymentTypeReadService;
import com.corevance.portfolio.savings.DepositAccountOnClosureType;
import com.corevance.portfolio.savings.DepositAccountType;
import com.corevance.portfolio.savings.data.DepositAccountData;
import com.corevance.portfolio.savings.data.DepositAccountTransactionDataValidator;
import com.corevance.portfolio.savings.data.FixedDepositAccountData;
import com.corevance.portfolio.savings.data.RecurringDepositAccountData;
import com.corevance.portfolio.savings.data.SavingsAccountData;
import com.corevance.portfolio.savings.domain.DepositAccountAssembler;
import com.corevance.portfolio.savings.domain.FixedDepositAccount;
import com.corevance.portfolio.savings.domain.RecurringDepositAccount;
import com.corevance.portfolio.savings.domain.SavingsAccount;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class DepositAccountPreMatureCalculationPlatformServiceImpl implements DepositAccountPreMatureCalculationPlatformService {

    private final FromJsonHelper fromJsonHelper;
    private final DepositAccountTransactionDataValidator depositAccountTransactionDataValidator;
    private final DepositAccountAssembler depositAccountAssembler;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final ConfigurationDomainService configurationDomainService;
    private final PaymentTypeReadService paymentTypeReadPlatformService;

    @Transactional
    @Override
    public DepositAccountData calculatePreMatureAmount(final Long accountId, final JsonQuery query,
            final DepositAccountType depositAccountType) {

        final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                .isSavingsInterestPostingAtCurrentPeriodEnd();
        final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

        this.depositAccountTransactionDataValidator.validatePreMatureAmountCalculation(query.json(), depositAccountType);
        final SavingsAccount account = this.depositAccountAssembler.assembleFrom(accountId, depositAccountType);

        DepositAccountData accountData = null;
        Collection<EnumOptionData> onAccountClosureOptions = SavingsEnumerations
                .depositAccountOnClosureType(new DepositAccountOnClosureType[] { DepositAccountOnClosureType.WITHDRAW_DEPOSIT,
                        DepositAccountOnClosureType.TRANSFER_TO_SAVINGS });
        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        final Collection<SavingsAccountData> savingsAccountDatas = this.savingsAccountReadPlatformService
                .retrieveActiveForLookup(account.clientId(), DepositAccountType.SAVINGS_DEPOSIT);
        final JsonElement element = this.fromJsonHelper.parse(query.json());
        final LocalDate preMaturityDate = this.fromJsonHelper.extractLocalDateNamed(closedOnDateParamName, element);
        // calculate interest before one day of closure date
        final LocalDate interestCalculatedToDate = preMaturityDate.minusDays(1);
        final boolean isPreMatureClosure = true;

        if (depositAccountType == DepositAccountType.FIXED_DEPOSIT) {
            final FixedDepositAccount fd = (FixedDepositAccount) account;
            accountData = FixedDepositAccountData.preClosureDetails(
                    account.getId(), fd.calculatePreMatureAmount(interestCalculatedToDate, isPreMatureClosure,
                            isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth),
                    onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas);
        } else if (depositAccountType == DepositAccountType.RECURRING_DEPOSIT) {
            final RecurringDepositAccount rd = (RecurringDepositAccount) account;
            accountData = RecurringDepositAccountData.preClosureDetails(
                    account.getId(), rd.calculatePreMatureAmount(interestCalculatedToDate, isPreMatureClosure,
                            isSavingsInterestPostingAtCurrentPeriodEnd, financialYearBeginningMonth),
                    onAccountClosureOptions, paymentTypeOptions, savingsAccountDatas);
        }

        return accountData;
    }
}
