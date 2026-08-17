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
package com.corevance.portfolio.rate.starter;

import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.portfolio.rate.domain.RateRepository;
import com.corevance.portfolio.rate.domain.RateRepositoryWrapper;
import com.corevance.portfolio.rate.serialization.RateDefinitionCommandFromApiJsonDeserializer;
import com.corevance.portfolio.rate.service.RateAssembler;
import com.corevance.portfolio.rate.service.RateReadService;
import com.corevance.portfolio.rate.service.RateReadServiceImpl;
import com.corevance.portfolio.rate.service.RateWriteService;
import com.corevance.portfolio.rate.service.RateWriteServiceImpl;
import com.corevance.useradministration.domain.AppUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RateConfiguration {

    @Bean
    @ConditionalOnMissingBean(RateAssembler.class)
    public RateAssembler rateAssembler(FromJsonHelper fromApiJsonHelper, RateRepositoryWrapper rateRepository) {
        return new RateAssembler(fromApiJsonHelper, rateRepository);
    }

    @Bean
    @ConditionalOnMissingBean(RateReadService.class)
    public RateReadService rateReadService(JdbcTemplate jdbcTemplate, PlatformSecurityContext context) {
        return new RateReadServiceImpl(jdbcTemplate, context);
    }

    @Bean
    @ConditionalOnMissingBean(RateWriteService.class)
    public RateWriteService rateWriteService(RateRepository rateRepository, AppUserRepository appUserRepository,
            PlatformSecurityContext context, RateDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        return new RateWriteServiceImpl(rateRepository, appUserRepository, context, fromApiJsonDeserializer);
    }
}
