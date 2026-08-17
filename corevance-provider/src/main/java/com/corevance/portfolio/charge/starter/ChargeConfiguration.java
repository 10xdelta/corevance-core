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
package com.corevance.portfolio.charge.starter;

import com.corevance.accounting.common.AccountingDropdownReadPlatformService;
import com.corevance.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainServiceJpa;
import com.corevance.infrastructure.entityaccess.service.CorevanceEntityAccessUtil;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.organisation.monetary.service.CurrencyReadPlatformService;
import com.corevance.portfolio.charge.domain.ChargeRepository;
import com.corevance.portfolio.charge.serialization.ChargeDefinitionCommandFromApiJsonDeserializer;
import com.corevance.portfolio.charge.service.ChargeDropdownReadPlatformService;
import com.corevance.portfolio.charge.service.ChargeDropdownReadPlatformServiceImpl;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.charge.service.ChargeReadPlatformServiceImpl;
import com.corevance.portfolio.charge.service.ChargeWritePlatformService;
import com.corevance.portfolio.charge.service.ChargeWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.common.service.DropdownReadPlatformService;
import com.corevance.portfolio.loanproduct.domain.LoanProductRepository;
import com.corevance.portfolio.paymenttype.domain.PaymentTypeRepository;
import com.corevance.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import com.corevance.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class ChargeConfiguration {

    @Bean
    @ConditionalOnMissingBean(ChargeDropdownReadPlatformService.class)
    public ChargeDropdownReadPlatformService chargeDropdownReadPlatformService() {
        return new ChargeDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ChargeReadPlatformService.class)
    public ChargeReadPlatformService chargeReadPlatformService(CurrencyReadPlatformService currencyReadPlatformService,
            ChargeDropdownReadPlatformService chargeDropdownReadPlatformService, JdbcTemplate jdbcTemplate,
            DropdownReadPlatformService dropdownReadPlatformService, CorevanceEntityAccessUtil corevanceEntityAccessUtil,
            AccountingDropdownReadPlatformService accountingDropdownReadPlatformService, TaxReadPlatformService taxReadPlatformService,
            ConfigurationDomainServiceJpa configurationDomainServiceJpa, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new ChargeReadPlatformServiceImpl(currencyReadPlatformService, chargeDropdownReadPlatformService, jdbcTemplate,
                dropdownReadPlatformService, corevanceEntityAccessUtil, accountingDropdownReadPlatformService, taxReadPlatformService,
                configurationDomainServiceJpa, namedParameterJdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ChargeWritePlatformService.class)
    public ChargeWritePlatformService chargeWritePlatformService(PlatformSecurityContext context,
            ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer, ChargeRepository chargeRepository,
            LoanProductRepository loanProductRepository, JdbcTemplate jdbcTemplate, CorevanceEntityAccessUtil corevanceEntityAccessUtil,
            GLAccountRepositoryWrapper glAccountRepository, TaxGroupRepositoryWrapper taxGroupRepository,
            PaymentTypeRepository paymentTypeRepository) {
        return new ChargeWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, chargeRepository, loanProductRepository,
                jdbcTemplate, corevanceEntityAccessUtil, glAccountRepository, taxGroupRepository, paymentTypeRepository);
    }
}
