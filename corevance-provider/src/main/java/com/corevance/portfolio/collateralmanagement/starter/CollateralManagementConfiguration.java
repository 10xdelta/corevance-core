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
package com.corevance.portfolio.collateralmanagement.starter;

import com.corevance.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.organisation.monetary.domain.ApplicationCurrencyRepository;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import com.corevance.portfolio.collateralmanagement.domain.CollateralManagementRepositoryWrapper;
import com.corevance.portfolio.collateralmanagement.service.ClientCollateralManagementReadService;
import com.corevance.portfolio.collateralmanagement.service.ClientCollateralManagementReadServiceImpl;
import com.corevance.portfolio.collateralmanagement.service.ClientCollateralManagementWriteService;
import com.corevance.portfolio.collateralmanagement.service.ClientCollateralManagementWriteServiceImpl;
import com.corevance.portfolio.collateralmanagement.service.CollateralManagementReadService;
import com.corevance.portfolio.collateralmanagement.service.CollateralManagementReadServiceImpl;
import com.corevance.portfolio.collateralmanagement.service.CollateralManagementWriteService;
import com.corevance.portfolio.collateralmanagement.service.CollateralManagementWriteServiceImpl;
import com.corevance.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import com.corevance.portfolio.collateralmanagement.service.LoanCollateralManagementReadService;
import com.corevance.portfolio.collateralmanagement.service.LoanCollateralManagementReadServiceImpl;
import com.corevance.portfolio.collateralmanagement.service.LoanCollateralManagementWriteService;
import com.corevance.portfolio.collateralmanagement.service.LoanCollateralManagementWriteServiceImpl;
import com.corevance.portfolio.loanaccount.domain.LoanCollateralManagementRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollateralManagementConfiguration {

    @Bean
    @ConditionalOnMissingBean(ClientCollateralManagementReadService.class)
    public ClientCollateralManagementReadService clientCollateralManagementReadService(
            ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper,
            LoanTransactionRepository loanTransactionRepository) {
        return new ClientCollateralManagementReadServiceImpl(clientCollateralManagementRepositoryWrapper, loanTransactionRepository);
    }

    @Bean
    @ConditionalOnMissingBean(ClientCollateralManagementWriteService.class)
    public ClientCollateralManagementWriteService clientCollateralManagementWriteService(
            ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper,
            CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper, ClientRepositoryWrapper clientRepositoryWrapper) {
        return new ClientCollateralManagementWriteServiceImpl(clientCollateralManagementRepositoryWrapper,
                collateralManagementRepositoryWrapper, clientRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(CollateralManagementReadService.class)
    public CollateralManagementReadService collateralManagementReadService(
            CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper) {
        return new CollateralManagementReadServiceImpl(collateralManagementRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(CollateralManagementWriteService.class)
    public CollateralManagementWriteService collateralManagementWriteService(
            CollateralManagementRepositoryWrapper collateralManagementRepositoryWrapper,
            ApplicationCurrencyRepository applicationCurrencyRepository) {
        return new CollateralManagementWriteServiceImpl(collateralManagementRepositoryWrapper, applicationCurrencyRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCollateralAssembler.class)
    public LoanCollateralAssembler loanCollateralAssembler(FromJsonHelper fromApiJsonHelper, CodeValueRepositoryWrapper codeValueRepository,
            LoanCollateralManagementRepository loanCollateralRepository,
            ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper) {
        return new LoanCollateralAssembler(fromApiJsonHelper, codeValueRepository, loanCollateralRepository,
                clientCollateralManagementRepositoryWrapper);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCollateralManagementReadService.class)
    public LoanCollateralManagementReadService loanCollateralManagementReadService(
            LoanCollateralManagementRepository loanCollateralManagementRepository, LoanRepository loanRepository) {
        return new LoanCollateralManagementReadServiceImpl(loanCollateralManagementRepository, loanRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanCollateralManagementWriteService.class)
    public LoanCollateralManagementWriteService loanCollateralManagementWriteService(
            LoanCollateralManagementRepository loanCollateralManagementRepository,
            ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper) {
        return new LoanCollateralManagementWriteServiceImpl(loanCollateralManagementRepository,
                clientCollateralManagementRepositoryWrapper);
    }
}
