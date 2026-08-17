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
package com.corevance.organisation.office.starter;

import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.security.service.InputValidator;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import com.corevance.organisation.monetary.service.CurrencyReadPlatformService;
import com.corevance.organisation.office.domain.OfficeRepository;
import com.corevance.organisation.office.domain.OfficeRepositoryWrapper;
import com.corevance.organisation.office.domain.OfficeTransactionRepository;
import com.corevance.organisation.office.mapper.OfficeDataMapper;
import com.corevance.organisation.office.serialization.OfficeCommandFromApiJsonDeserializer;
import com.corevance.organisation.office.serialization.OfficeTransactionCommandFromApiJsonDeserializer;
import com.corevance.organisation.office.service.OfficeReadPlatformService;
import com.corevance.organisation.office.service.OfficeReadPlatformServiceImpl;
import com.corevance.organisation.office.service.OfficeWritePlatformService;
import com.corevance.organisation.office.service.OfficeWritePlatformServiceJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class OrganisationOfficeConfiguration {

    @Bean
    @ConditionalOnMissingBean(OfficeReadPlatformService.class)
    public OfficeReadPlatformService officeReadPlatformService(JdbcTemplate jdbcTemplate, DatabaseSpecificSQLGenerator sqlGenerator,
            PlatformSecurityContext context, CurrencyReadPlatformService currencyReadPlatformService, InputValidator inputValidator,
            OfficeRepository officeRepository, OfficeDataMapper officeDataMapper) {
        return new OfficeReadPlatformServiceImpl(jdbcTemplate, sqlGenerator, context, currencyReadPlatformService, inputValidator,
                officeRepository, officeDataMapper);
    }

    @Bean
    @ConditionalOnMissingBean(OfficeWritePlatformService.class)
    public OfficeWritePlatformService officeWritePlatformService(PlatformSecurityContext context,
            OfficeCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            OfficeTransactionCommandFromApiJsonDeserializer moneyTransferCommandFromApiJsonDeserializer,
            OfficeRepositoryWrapper officeRepositoryWrapper, OfficeTransactionRepository officeTransactionRepository,
            ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository) {
        return new OfficeWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer,
                moneyTransferCommandFromApiJsonDeserializer, officeRepositoryWrapper, officeTransactionRepository,
                applicationCurrencyRepository);
    }
}
