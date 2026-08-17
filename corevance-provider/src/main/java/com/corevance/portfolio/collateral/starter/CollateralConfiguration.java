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
package com.corevance.portfolio.collateral.starter;

import com.corevance.infrastructure.codes.domain.CodeValueRepository;
import com.corevance.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.portfolio.collateral.domain.LoanCollateralRepository;
import com.corevance.portfolio.collateral.serialization.CollateralCommandFromApiJsonDeserializer;
import com.corevance.portfolio.collateral.service.CollateralAssembler;
import com.corevance.portfolio.collateral.service.CollateralReadPlatformService;
import com.corevance.portfolio.collateral.service.CollateralReadPlatformServiceImpl;
import com.corevance.portfolio.collateral.service.CollateralWritePlatformService;
import com.corevance.portfolio.collateral.service.CollateralWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CollateralConfiguration {

    @Bean
    @ConditionalOnMissingBean(CollateralAssembler.class)
    public CollateralAssembler collateralAssembler(FromJsonHelper fromApiJsonHelper, CodeValueRepositoryWrapper codeValueRepository,
            CodeValueRepository codeValueRepositoryDirect, LoanCollateralRepository loanCollateralRepository) {
        return new CollateralAssembler(fromApiJsonHelper, codeValueRepository, codeValueRepositoryDirect, loanCollateralRepository);
    }

    @Bean
    @ConditionalOnMissingBean(CollateralReadPlatformService.class)
    public CollateralReadPlatformService collateralReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            LoanRepositoryWrapper loanRepositoryWrapper) {
        return new CollateralReadPlatformServiceImpl(context, jdbcTemplate, loanRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(CollateralWritePlatformService.class)
    public CollateralWritePlatformService collateralWritePlatformService(PlatformSecurityContext context,
            LoanRepositoryWrapper loanRepositoryWrapper, LoanCollateralRepository collateralRepository,
            CodeValueRepositoryWrapper codeValueRepository,
            CollateralCommandFromApiJsonDeserializer collateralCommandFromApiJsonDeserializer) {
        return new CollateralWritePlatformServiceJpaRepositoryImpl(context, loanRepositoryWrapper, collateralRepository,
                codeValueRepository, collateralCommandFromApiJsonDeserializer);
    }

}
