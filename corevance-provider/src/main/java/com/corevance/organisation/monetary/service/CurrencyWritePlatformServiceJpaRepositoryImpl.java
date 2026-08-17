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
package com.corevance.organisation.monetary.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import com.corevance.organisation.monetary.data.CurrencyUpdateRequest;
import com.corevance.organisation.monetary.data.CurrencyUpdateResponse;
import com.corevance.organisation.monetary.domain.ApplicationCurrency;
import com.corevance.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import com.corevance.organisation.monetary.domain.OrganisationCurrency;
import com.corevance.organisation.monetary.domain.OrganisationCurrencyRepository;
import com.corevance.organisation.monetary.exception.CurrencyInUseException;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.loanproduct.service.LoanProductReadPlatformService;
import com.corevance.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CurrencyWritePlatformServiceJpaRepositoryImpl implements CurrencyWritePlatformService {

    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final OrganisationCurrencyRepository organisationCurrencyRepository;
    private final LoanProductReadPlatformService loanProductService;
    private final SavingsProductReadPlatformService savingsProductService;
    private final ChargeReadPlatformService chargeService;

    @Transactional
    @Override
    public CurrencyUpdateResponse updateAllowedCurrencies(final CurrencyUpdateRequest request) {
        final var currencies = request.getCurrencies();

        final List<String> allowedCurrencyCodes = new ArrayList<>();
        final Set<OrganisationCurrency> allowedCurrencies = new HashSet<>();
        for (final String currencyCode : currencies) {

            final ApplicationCurrency currency = applicationCurrencyRepository.findOneWithNotFoundDetection(currencyCode);

            final OrganisationCurrency allowedCurrency = currency.toOrganisationCurrency();

            allowedCurrencyCodes.add(currencyCode);
            allowedCurrencies.add(allowedCurrency);
        }

        for (OrganisationCurrency priorCurrency : organisationCurrencyRepository.findAll()) {
            if (!allowedCurrencyCodes.contains(priorCurrency.getCode())) {
                // check if it's safe to remove this currency.
                if (!loanProductService.retrieveAllLoanProductsForCurrency(priorCurrency.getCode()).isEmpty()
                        || !savingsProductService.retrieveAllForCurrency(priorCurrency.getCode()).isEmpty()
                        || !chargeService.retrieveAllChargesForCurrency(priorCurrency.getCode()).isEmpty()) {
                    throw new CurrencyInUseException(priorCurrency.getCode());
                }
            }
        }

        organisationCurrencyRepository.deleteAll();
        organisationCurrencyRepository.saveAll(allowedCurrencies);

        return CurrencyUpdateResponse.builder().currencies(allowedCurrencyCodes).build();
    }
}
