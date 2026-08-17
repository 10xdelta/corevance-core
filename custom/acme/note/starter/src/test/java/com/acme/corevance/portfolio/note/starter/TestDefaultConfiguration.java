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
package com.acme.corevance.portfolio.note.starter;

import static org.mockito.Mockito.mock;

import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.service.database.RoutingDataSource;
import com.corevance.infrastructure.core.service.database.RoutingDataSourceServiceFactory;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.portfolio.group.domain.GroupRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

@EnableConfigurationProperties({ CorevanceProperties.class })
@ComponentScan("com.corevance.portfolio.note.service")
class TestDefaultConfiguration {
    // NOTE: unfortunately an abastract base class that contains all these mock functions won't work

    @Bean
    RoutingDataSourceServiceFactory routingDataSourceServiceFactory() {
        return mock(RoutingDataSourceServiceFactory.class);
    }

    @Bean
    RoutingDataSource routingDataSource() {
        return mock(RoutingDataSource.class);
    }

    @Bean
    JdbcTemplate jdbcTemplate() {
        return mock(JdbcTemplate.class);
    }

    @Bean
    NoteRepository noteRepository() {
        return mock(NoteRepository.class);
    }

    @Bean
    ClientRepositoryWrapper clientRepository() {
        return mock(ClientRepositoryWrapper.class);
    }

    @Bean
    GroupRepository groupRepository() {
        return mock(GroupRepository.class);
    }

    @Bean
    LoanRepositoryWrapper loanRepository() {
        return mock(LoanRepositoryWrapper.class);
    }

    @Bean
    LoanTransactionRepository loanTransactionRepository() {
        return mock(LoanTransactionRepository.class);
    }

    @Bean
    SavingsAccountRepository savingsAccountRepository() {
        return mock(SavingsAccountRepository.class);
    }
}
