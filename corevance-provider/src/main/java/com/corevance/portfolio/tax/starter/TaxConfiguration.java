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
package com.corevance.portfolio.tax.starter;

import com.corevance.accounting.common.AccountingDropdownReadPlatformService;
import com.corevance.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.portfolio.tax.domain.TaxComponentRepository;
import com.corevance.portfolio.tax.domain.TaxComponentRepositoryWrapper;
import com.corevance.portfolio.tax.domain.TaxGroupRepository;
import com.corevance.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import com.corevance.portfolio.tax.mapper.TaxComponentMapper;
import com.corevance.portfolio.tax.mapper.TaxGroupMapper;
import com.corevance.portfolio.tax.serialization.TaxValidator;
import com.corevance.portfolio.tax.service.TaxAssembler;
import com.corevance.portfolio.tax.service.TaxReadPlatformService;
import com.corevance.portfolio.tax.service.TaxReadPlatformServiceImpl;
import com.corevance.portfolio.tax.service.TaxWritePlatformService;
import com.corevance.portfolio.tax.service.TaxWritePlatformServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaxConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaxAssembler.class)
    public TaxAssembler taxAssembler(FromJsonHelper fromApiJsonHelper, GLAccountRepositoryWrapper glAccountRepositoryWrapper,
            TaxComponentRepositoryWrapper taxComponentRepositoryWrapper) {
        return new TaxAssembler(fromApiJsonHelper, glAccountRepositoryWrapper, taxComponentRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(TaxReadPlatformService.class)
    public TaxReadPlatformService taxReadPlatformService(final TaxComponentRepository taxComponentRepository,
            final TaxComponentRepositoryWrapper taxComponentRepositoryWrapper, final TaxComponentMapper taxComponentMapper,
            final TaxGroupRepository taxGroupRepository, final TaxGroupRepositoryWrapper taxGroupRepositoryWrapper,
            final TaxGroupMapper taxGroupMapper, AccountingDropdownReadPlatformService accountingDropdownReadPlatformService) {
        return new TaxReadPlatformServiceImpl(accountingDropdownReadPlatformService, taxComponentRepository, taxComponentRepositoryWrapper,
                taxComponentMapper, taxGroupRepository, taxGroupRepositoryWrapper, taxGroupMapper);
    }

    @Bean
    @ConditionalOnMissingBean(TaxWritePlatformService.class)
    public TaxWritePlatformService taxWritePlatformService(TaxValidator validator, TaxAssembler taxAssembler,
            TaxComponentRepository taxComponentRepository, TaxGroupRepository taxGroupRepository,
            TaxComponentRepositoryWrapper taxComponentRepositoryWrapper, TaxGroupRepositoryWrapper taxGroupRepositoryWrapper) {
        return new TaxWritePlatformServiceImpl(validator, taxAssembler, taxComponentRepository, taxComponentRepositoryWrapper,
                taxGroupRepository, taxGroupRepositoryWrapper);
    }
}
