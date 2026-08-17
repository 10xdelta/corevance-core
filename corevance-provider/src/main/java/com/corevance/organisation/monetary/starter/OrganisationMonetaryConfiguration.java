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
package com.corevance.organisation.monetary.starter;

import com.corevance.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import com.corevance.organisation.monetary.domain.OrganisationCurrencyRepository;
import com.corevance.organisation.monetary.service.CurrencyReadPlatformService;
import com.corevance.organisation.monetary.service.CurrencyReadPlatformServiceImpl;
import com.corevance.organisation.monetary.service.CurrencyWritePlatformService;
import com.corevance.organisation.monetary.service.CurrencyWritePlatformServiceJpaRepositoryImpl;
import com.corevance.organisation.monetary.service.OrganisationCurrencyReadPlatformService;
import com.corevance.organisation.monetary.service.OrganisationCurrencyReadPlatformServiceImpl;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.loanproduct.service.LoanProductReadPlatformService;
import com.corevance.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OrganisationMonetaryConfiguration {

    @Bean
    @ConditionalOnMissingBean(CurrencyReadPlatformService.class)
    public CurrencyReadPlatformService currencyReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new CurrencyReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(CurrencyWritePlatformService.class)
    public CurrencyWritePlatformService currencyWritePlatformService(ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            OrganisationCurrencyRepository organisationCurrencyRepository, LoanProductReadPlatformService loanProductService,
            SavingsProductReadPlatformService savingsProductService, ChargeReadPlatformService chargeService) {
        return new CurrencyWritePlatformServiceJpaRepositoryImpl(applicationCurrencyRepository, organisationCurrencyRepository,
                loanProductService, savingsProductService, chargeService);
    }

    @Bean
    @ConditionalOnMissingBean(OrganisationCurrencyReadPlatformService.class)
    public OrganisationCurrencyReadPlatformService organisationCurrencyReadPlatformService(
            CurrencyReadPlatformService currencyReadPlatformService) {
        return new OrganisationCurrencyReadPlatformServiceImpl(currencyReadPlatformService);
    }
}
