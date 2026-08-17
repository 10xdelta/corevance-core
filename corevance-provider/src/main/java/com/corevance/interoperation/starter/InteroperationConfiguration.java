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
package com.corevance.interoperation.starter;

import com.corevance.commands.service.PortfolioCommandSourceWritePlatformService;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.interoperation.domain.InteropIdentifierRepository;
import com.corevance.interoperation.serialization.InteropDataValidator;
import com.corevance.interoperation.service.InteropService;
import com.corevance.interoperation.service.InteropServiceImpl;
import com.corevance.organisation.monetary.domain.ApplicationCurrencyRepository;
import com.corevance.portfolio.loanaccount.data.LoanAccountData;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.paymenttype.domain.PaymentTypeRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountTransactionRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import com.corevance.portfolio.savings.domain.SavingsHelper;
import com.corevance.portfolio.savings.service.SavingsAccountDomainService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class InteroperationConfiguration {

    @Bean
    @ConditionalOnMissingBean(InteropService.class)
    public InteropService interopService(PlatformSecurityContext securityContext, InteropDataValidator interopDataValidator,
            SavingsAccountRepository savingsAccountRepository, SavingsAccountTransactionRepository savingsAccountTransactionRepository,
            ApplicationCurrencyRepository applicationCurrencyRepository, NoteRepository noteRepository,
            PaymentTypeRepository paymentTypeRepository, InteropIdentifierRepository identifierRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, SavingsHelper savingsHelper,
            SavingsAccountTransactionSummaryWrapper savingsAccountTransactionSummaryWrapper,
            SavingsAccountDomainService savingsAccountService, ConfigurationDomainService configurationDomainService,
            JdbcTemplate jdbcTemplate, PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new InteropServiceImpl(securityContext, interopDataValidator, savingsAccountRepository, savingsAccountTransactionRepository,
                applicationCurrencyRepository, noteRepository, paymentTypeRepository, identifierRepository, loanRepositoryWrapper,
                savingsHelper, savingsAccountTransactionSummaryWrapper, savingsAccountService, configurationDomainService, jdbcTemplate,
                commandsSourceWritePlatformService, toApiJsonSerializer, sqlGenerator);
    }
}
