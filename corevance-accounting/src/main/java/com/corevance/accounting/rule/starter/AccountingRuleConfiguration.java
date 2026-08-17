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
package com.corevance.accounting.rule.starter;

import com.corevance.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import com.corevance.accounting.glaccount.service.GLAccountReadPlatformService;
import com.corevance.accounting.rule.domain.AccountingRuleRepository;
import com.corevance.accounting.rule.domain.AccountingRuleRepositoryWrapper;
import com.corevance.accounting.rule.serialization.AccountingRuleCommandFromApiJsonDeserializer;
import com.corevance.accounting.rule.service.AccountingRuleReadPlatformService;
import com.corevance.accounting.rule.service.AccountingRuleReadPlatformServiceImpl;
import com.corevance.accounting.rule.service.AccountingRuleWritePlatformService;
import com.corevance.accounting.rule.service.AccountingRuleWritePlatformServiceJpaRepositoryImpl;
import com.corevance.infrastructure.codes.domain.CodeValueRepository;
import com.corevance.organisation.office.domain.OfficeRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AccountingRuleConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccountingRuleReadPlatformService.class)
    public AccountingRuleReadPlatformService accountingRuleReadPlatformService(JdbcTemplate jdbcTemplate,
            GLAccountReadPlatformService glAccountReadPlatformService) {
        return new AccountingRuleReadPlatformServiceImpl(jdbcTemplate, glAccountReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(AccountingRuleWritePlatformService.class)
    public AccountingRuleWritePlatformService accountingRuleWritePlatformService(
            AccountingRuleRepositoryWrapper accountingRuleRepositoryWrapper, AccountingRuleRepository accountingRuleRepository,
            GLAccountRepositoryWrapper accountRepositoryWrapper, OfficeRepositoryWrapper officeRepositoryWrapper,
            AccountingRuleCommandFromApiJsonDeserializer fromApiJsonDeserializer, CodeValueRepository codeValueRepository) {
        return new AccountingRuleWritePlatformServiceJpaRepositoryImpl(accountingRuleRepositoryWrapper, accountingRuleRepository,
                accountRepositoryWrapper, officeRepositoryWrapper, fromApiJsonDeserializer, codeValueRepository);
    }

}
