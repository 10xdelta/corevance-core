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
package com.corevance.portfolio.loanproduct.starter;

import com.corevance.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.entityaccess.service.CorevanceEntityAccessUtil;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.portfolio.charge.domain.ChargeRepositoryWrapper;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucketRepository;
import com.corevance.portfolio.delinquency.service.DelinquencyReadPlatformService;
import com.corevance.portfolio.floatingrates.domain.FloatingRateRepositoryWrapper;
import com.corevance.portfolio.fund.domain.FundRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import com.corevance.portfolio.loanaccount.service.LoanProductAssembler;
import com.corevance.portfolio.loanaccount.service.LoanProductUpdateUtil;
import com.corevance.portfolio.loanproduct.domain.AdvancedPaymentAllocationsJsonParser;
import com.corevance.portfolio.loanproduct.domain.CreditAllocationsJsonParser;
import com.corevance.portfolio.loanproduct.domain.LoanProductRepository;
import com.corevance.portfolio.loanproduct.serialization.LoanProductDataValidator;
import com.corevance.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import com.corevance.portfolio.loanproduct.service.LoanDropdownReadPlatformServiceImpl;
import com.corevance.portfolio.loanproduct.service.LoanProductReadPlatformService;
import com.corevance.portfolio.loanproduct.service.LoanProductReadPlatformServiceImpl;
import com.corevance.portfolio.loanproduct.service.LoanProductWritePlatformService;
import com.corevance.portfolio.loanproduct.service.LoanProductWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.rate.domain.RateRepositoryWrapper;
import com.corevance.portfolio.rate.service.RateReadService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class LoanProductConfiguration {

    @Bean
    @ConditionalOnMissingBean(LoanDropdownReadPlatformService.class)
    public LoanDropdownReadPlatformService loanDropdownReadPlatformService(
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory) {
        return new LoanDropdownReadPlatformServiceImpl(loanRepaymentScheduleTransactionProcessorFactory);
    }

    @Bean
    @ConditionalOnMissingBean(LoanProductReadPlatformService.class)
    public LoanProductReadPlatformService loanProductReadPlatformService(PlatformSecurityContext context, JdbcTemplate jdbcTemplate,
            ChargeReadPlatformService chargeReadPlatformService, RateReadService rateReadService, DatabaseSpecificSQLGenerator sqlGenerator,
            CorevanceEntityAccessUtil corevanceEntityAccessUtil, DelinquencyReadPlatformService delinquencyReadPlatformService,
            LoanProductRepository loanProductRepository) {
        return new LoanProductReadPlatformServiceImpl(context, jdbcTemplate, chargeReadPlatformService, rateReadService, sqlGenerator,
                corevanceEntityAccessUtil, delinquencyReadPlatformService, loanProductRepository);
    }

    @Bean
    @ConditionalOnMissingBean(LoanProductWritePlatformService.class)
    public LoanProductWritePlatformService loanProductWritePlatformService(PlatformSecurityContext context,
            LoanProductDataValidator fromApiJsonDeserializer, LoanProductRepository loanProductRepository, AprCalculator aprCalculator,
            FundRepository fundRepository, ChargeRepositoryWrapper chargeRepository, RateRepositoryWrapper rateRepository,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
            CorevanceEntityAccessUtil corevanceEntityAccessUtil, FloatingRateRepositoryWrapper floatingRateRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, BusinessEventNotifierService businessEventNotifierService,
            DelinquencyBucketRepository delinquencyBucketRepository,
            LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            AdvancedPaymentAllocationsJsonParser advancedPaymentJsonParser, CreditAllocationsJsonParser creditAllocationsJsonParser,
            LoanProductAssembler loanProductAssembler, LoanProductUpdateUtil loanProductUpdateUtil) {
        return new LoanProductWritePlatformServiceJpaRepositoryImpl(context, fromApiJsonDeserializer, loanProductRepository, aprCalculator,
                fundRepository, chargeRepository, rateRepository, accountMappingWritePlatformService, corevanceEntityAccessUtil,
                floatingRateRepository, loanRepositoryWrapper, businessEventNotifierService, delinquencyBucketRepository,
                loanRepaymentScheduleTransactionProcessorFactory, advancedPaymentJsonParser, creditAllocationsJsonParser,
                loanProductAssembler, loanProductUpdateUtil);
    }
}
