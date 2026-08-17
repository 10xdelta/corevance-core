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
package com.acme.corevance.loan.starter;

import static org.mockito.Mockito.mock;

import com.corevance.cob.COBBusinessStepService;
import com.corevance.cob.COBBusinessStepServiceImpl;
import com.corevance.cob.domain.BatchBusinessStepRepository;
import com.corevance.cob.service.ReloaderService;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.diagnostics.performance.sampling.core.SamplingConfiguration;
import com.corevance.infrastructure.core.diagnostics.performance.sampling.core.SamplingServiceFactory;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties({ CorevanceProperties.class })
public class TestDefaultConfiguration {

    @Bean
    public BatchBusinessStepRepository batchBusinessStepRepository() {
        return mock(BatchBusinessStepRepository.class);
    }

    @Bean
    public BusinessEventNotifierService businessEventNotifierService() {
        return mock(BusinessEventNotifierService.class);
    }

    @Bean
    public COBBusinessStepService cobBusinessStepService(BatchBusinessStepRepository batchBusinessStepRepository,
            ApplicationContext context, ListableBeanFactory beanFactory, BusinessEventNotifierService businessEventNotifierService,
            ConfigurationDomainService configurationDomainService, ReloaderService reloaderService) {
        return new COBBusinessStepServiceImpl(batchBusinessStepRepository, context, beanFactory, businessEventNotifierService,
                configurationDomainService, reloaderService);
    }

    @Bean
    public SamplingServiceFactory samplingServiceFactory(SamplingConfiguration samplingConfiguration) {
        return new SamplingServiceFactory(samplingConfiguration);
    }

    @Bean
    public LoanAccountDomainService loanAccountDomainService() {
        return mock(LoanAccountDomainService.class);
    }
}
