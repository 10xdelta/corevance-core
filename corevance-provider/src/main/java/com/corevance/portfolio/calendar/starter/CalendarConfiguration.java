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
package com.corevance.portfolio.calendar.starter;

import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.portfolio.calendar.domain.CalendarHistoryRepository;
import com.corevance.portfolio.calendar.domain.CalendarInstanceRepository;
import com.corevance.portfolio.calendar.domain.CalendarRepository;
import com.corevance.portfolio.calendar.serialization.CalendarCommandFromApiJsonDeserializer;
import com.corevance.portfolio.calendar.service.CalendarDropdownReadPlatformService;
import com.corevance.portfolio.calendar.service.CalendarDropdownReadPlatformServiceImpl;
import com.corevance.portfolio.calendar.service.CalendarReadPlatformService;
import com.corevance.portfolio.calendar.service.CalendarReadPlatformServiceImpl;
import com.corevance.portfolio.calendar.service.CalendarWritePlatformService;
import com.corevance.portfolio.calendar.service.CalendarWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.portfolio.group.domain.GroupRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.service.LoanWritePlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CalendarConfiguration {

    @Bean
    @ConditionalOnMissingBean(CalendarDropdownReadPlatformService.class)
    public CalendarDropdownReadPlatformService calendarDropdownReadPlatformService() {
        return new CalendarDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(CalendarReadPlatformService.class)
    public CalendarReadPlatformService calendarReadPlatformService(JdbcTemplate jdbcTemplate,
            ConfigurationDomainService configurationDomainService) {
        return new CalendarReadPlatformServiceImpl(jdbcTemplate, configurationDomainService);
    }

    @Bean
    @ConditionalOnMissingBean(CalendarWritePlatformService.class)
    public CalendarWritePlatformService calendarWritePlatformService(CalendarRepository calendarRepository,
            CalendarHistoryRepository calendarHistoryRepository, CalendarCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            CalendarInstanceRepository calendarInstanceRepository, LoanWritePlatformService loanWritePlatformService,
            ConfigurationDomainService configurationDomainService, GroupRepositoryWrapper groupRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, ClientRepositoryWrapper clientRepository) {
        return new CalendarWritePlatformServiceJpaRepositoryImpl(calendarRepository, calendarHistoryRepository, fromApiJsonDeserializer,
                calendarInstanceRepository, loanWritePlatformService, configurationDomainService, groupRepository, loanRepositoryWrapper,
                clientRepository);
    }
}
