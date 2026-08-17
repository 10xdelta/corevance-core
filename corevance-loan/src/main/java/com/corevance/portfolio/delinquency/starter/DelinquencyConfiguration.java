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
package com.corevance.portfolio.delinquency.starter;

import java.util.List;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucketMappingsRepository;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucketRepository;
import com.corevance.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRuleRepository;
import com.corevance.portfolio.delinquency.domain.DelinquencyRangeRepository;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import com.corevance.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import com.corevance.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import com.corevance.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import com.corevance.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import com.corevance.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import com.corevance.portfolio.delinquency.service.DelinquencyReadPlatformService;
import com.corevance.portfolio.delinquency.service.DelinquencyReadPlatformServiceImpl;
import com.corevance.portfolio.delinquency.service.DelinquencyWritePlatformService;
import com.corevance.portfolio.delinquency.service.DelinquencyWritePlatformServiceHelper;
import com.corevance.portfolio.delinquency.service.DelinquencyWritePlatformServiceImpl;
import com.corevance.portfolio.delinquency.service.LoanDelinquencyDomainService;
import com.corevance.portfolio.delinquency.service.LoanDelinquencyDomainServiceImpl;
import com.corevance.portfolio.delinquency.service.PossibleNextRepaymentCalculationServiceDiscovery;
import com.corevance.portfolio.delinquency.spi.DelinquencyBucketUsageChecker;
import com.corevance.portfolio.delinquency.validator.DelinquencyActionParseAndValidator;
import com.corevance.portfolio.delinquency.validator.DelinquencyBucketParseAndValidator;
import com.corevance.portfolio.delinquency.validator.DelinquencyRangeParseAndValidator;
import com.corevance.portfolio.loanaccount.domain.LoanRepository;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.service.LoanTransactionReadService;
import com.corevance.portfolio.loanproduct.domain.LoanProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DelinquencyConfiguration {

    @Bean
    @ConditionalOnMissingBean(DelinquencyReadPlatformService.class)
    public DelinquencyReadPlatformService delinquencyReadPlatformService(DelinquencyRangeRepository repositoryRange,
            DelinquencyBucketRepository repositoryBucket, LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory,
            DelinquencyMinimumPaymentPeriodAndRuleRepository minimumPaymentPeriodAndRuleRepository, DelinquencyRangeMapper mapperRange,
            DelinquencyBucketMapper mapperBucket, LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory, LoanRepository loanRepository,
            LoanDelinquencyDomainService loanDelinquencyDomainService,
            LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag,
            LoanDelinquencyActionRepository loanDelinquencyActionRepository,
            DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper, ConfigurationDomainService configurationDomainService,
            LoanTransactionRepository loanTransactionRepository,
            PossibleNextRepaymentCalculationServiceDiscovery possibleNextRepaymentCalculationService) {
        return new DelinquencyReadPlatformServiceImpl(repositoryRange, repositoryBucket, minimumPaymentPeriodAndRuleRepository,
                repositoryLoanDelinquencyTagHistory, mapperRange, mapperBucket, mapperLoanDelinquencyTagHistory, loanRepository,
                loanDelinquencyDomainService, repositoryLoanInstallmentDelinquencyTag, loanDelinquencyActionRepository,
                delinquencyEffectivePauseHelper, configurationDomainService, loanTransactionRepository,
                possibleNextRepaymentCalculationService);
    }

    @Bean
    @ConditionalOnMissingBean(DelinquencyWritePlatformService.class)
    public DelinquencyWritePlatformService delinquencyWritePlatformService(DelinquencyBucketParseAndValidator dataValidatorBucket,
            DelinquencyRangeParseAndValidator dataValidatorRange, DelinquencyRangeRepository repositoryRange,
            DelinquencyBucketRepository repositoryBucket, DelinquencyBucketMappingsRepository repositoryBucketMappings,
            LoanDelinquencyTagHistoryRepository loanDelinquencyTagRepository, LoanRepositoryWrapper loanRepository,
            LoanProductRepository loanProductRepository, BusinessEventNotifierService businessEventNotifierService,
            LoanDelinquencyDomainService loanDelinquencyDomainService,
            LoanInstallmentDelinquencyTagRepository loanInstallmentDelinquencyTagRepository,
            DelinquencyReadPlatformService delinquencyReadPlatformService, LoanDelinquencyActionRepository loanDelinquencyActionRepository,
            DelinquencyActionParseAndValidator delinquencyActionParseAndValidator,
            DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper,
            DelinquencyWritePlatformServiceHelper delinquencyWritePlatformServiceHelper,
            DelinquencyMinimumPaymentPeriodAndRuleRepository delinquencyMinimumPaymentPeriodAndRuleRepository,
            final List<DelinquencyBucketUsageChecker> delinquencyBucketUsageCheckers) {
        return new DelinquencyWritePlatformServiceImpl(dataValidatorBucket, dataValidatorRange, repositoryRange, repositoryBucket,
                repositoryBucketMappings, loanDelinquencyTagRepository, loanRepository, loanProductRepository, loanDelinquencyDomainService,
                loanInstallmentDelinquencyTagRepository, delinquencyReadPlatformService, loanDelinquencyActionRepository,
                delinquencyActionParseAndValidator, delinquencyEffectivePauseHelper, businessEventNotifierService,
                delinquencyWritePlatformServiceHelper, delinquencyMinimumPaymentPeriodAndRuleRepository, delinquencyBucketUsageCheckers);
    }

    @Bean
    @ConditionalOnMissingBean(LoanDelinquencyDomainService.class)
    public LoanDelinquencyDomainService loanDelinquencyDomainService(DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper,
            LoanTransactionReadService loanTransactionReadService) {
        return new LoanDelinquencyDomainServiceImpl(delinquencyEffectivePauseHelper, loanTransactionReadService);
    }
}
