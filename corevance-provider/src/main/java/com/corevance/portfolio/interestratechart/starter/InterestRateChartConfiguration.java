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

package com.corevance.portfolio.interestratechart.starter;

import com.corevance.infrastructure.codes.service.CodeValueReadPlatformService;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.portfolio.interestratechart.domain.InterestRateChartRepositoryWrapper;
import com.corevance.portfolio.interestratechart.domain.InterestRateChartSlabRepository;
import com.corevance.portfolio.interestratechart.service.InterestIncentiveAssembler;
import com.corevance.portfolio.interestratechart.service.InterestIncentiveDropdownReadService;
import com.corevance.portfolio.interestratechart.service.InterestIncentivesDropdownReadServiceImpl;
import com.corevance.portfolio.interestratechart.service.InterestRateChartAssembler;
import com.corevance.portfolio.interestratechart.service.InterestRateChartDropdownReadService;
import com.corevance.portfolio.interestratechart.service.InterestRateChartDropdownReadServiceImpl;
import com.corevance.portfolio.interestratechart.service.InterestRateChartReadService;
import com.corevance.portfolio.interestratechart.service.InterestRateChartReadServiceImpl;
import com.corevance.portfolio.interestratechart.service.InterestRateChartSlabAssembler;
import com.corevance.portfolio.interestratechart.service.InterestRateChartSlabsReadService;
import com.corevance.portfolio.interestratechart.service.InterestRateChartSlabsReadServiceImpl;
import com.corevance.portfolio.interestratechart.service.InterestRateChartSlabsWriteService;
import com.corevance.portfolio.interestratechart.service.InterestRateChartSlabsWriteServiceImpl;
import com.corevance.portfolio.interestratechart.service.InterestRateChartWriteService;
import com.corevance.portfolio.interestratechart.service.InterestRateChartWriteServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class InterestRateChartConfiguration {

    @Bean
    @ConditionalOnMissingBean(InterestIncentiveAssembler.class)
    public InterestIncentiveAssembler interestIncentiveAssembler(FromJsonHelper fromApiJsonHelper) {
        return new InterestIncentiveAssembler(fromApiJsonHelper);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartAssembler.class)
    public InterestRateChartAssembler interestRateChartAssembler(FromJsonHelper fromApiJsonHelper,
            InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper, InterestRateChartSlabAssembler chartSlabAssembler) {
        return new InterestRateChartAssembler(fromApiJsonHelper, interestRateChartRepositoryWrapper, chartSlabAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartSlabAssembler.class)
    public InterestRateChartSlabAssembler interestRateChartSlabAssembler(FromJsonHelper fromApiJsonHelper,
            InterestRateChartRepositoryWrapper interestRateChartRepositoryWrapper, InterestIncentiveAssembler incentiveAssembler) {
        return new InterestRateChartSlabAssembler(fromApiJsonHelper, interestRateChartRepositoryWrapper, incentiveAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(InterestIncentiveDropdownReadService.class)
    public InterestIncentiveDropdownReadService interestIncentiveDropdownReadService(

    ) {
        return new InterestIncentivesDropdownReadServiceImpl(

        );
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartDropdownReadService.class)
    public InterestRateChartDropdownReadService interestRateChartDropdownReadService(

    ) {
        return new InterestRateChartDropdownReadServiceImpl(

        );
    }

    @Bean
    public InterestRateChartReadServiceImpl.InterestRateChartExtractor interestRateChartExtractor(
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new InterestRateChartReadServiceImpl.InterestRateChartExtractor(sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartReadService.class)
    public InterestRateChartReadService interestRateChartReadService(JdbcTemplate jdbcTemplate,
            InterestRateChartReadServiceImpl.InterestRateChartExtractor chartExtractor,
            InterestRateChartDropdownReadService chartDropdownReadPlatformService,
            InterestIncentiveDropdownReadService interestIncentiveDropdownReadService,
            CodeValueReadPlatformService codeValueReadPlatformService) {
        return new InterestRateChartReadServiceImpl(jdbcTemplate, chartExtractor, chartDropdownReadPlatformService,
                interestIncentiveDropdownReadService, codeValueReadPlatformService);
    }

    @Bean
    public InterestRateChartSlabsReadServiceImpl.InterestRateChartSlabExtractor interestRateChartSlabExtractor(
            DatabaseSpecificSQLGenerator sqlGenerator) {
        return new InterestRateChartSlabsReadServiceImpl.InterestRateChartSlabExtractor(sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartSlabsReadService.class)
    public InterestRateChartSlabsReadService interestRateChartSlabReadService(JdbcTemplate jdbcTemplate,
            InterestRateChartSlabsReadServiceImpl.InterestRateChartSlabExtractor chartSlabExtractor,
            InterestRateChartDropdownReadService chartDropdownReadPlatformService,
            InterestIncentiveDropdownReadService interestIncentiveDropdownReadService,
            CodeValueReadPlatformService codeValueReadPlatformService) {
        return new InterestRateChartSlabsReadServiceImpl(jdbcTemplate, chartSlabExtractor, chartDropdownReadPlatformService,
                interestIncentiveDropdownReadService, codeValueReadPlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartSlabsWriteService.class)
    public InterestRateChartSlabsWriteService interestRateChartSlabWriteService(
            InterestRateChartRepositoryWrapper interestRateChartRepository, InterestRateChartSlabRepository chartSlabRepository) {
        return new InterestRateChartSlabsWriteServiceImpl(interestRateChartRepository, chartSlabRepository);
    }

    @Bean
    @ConditionalOnMissingBean(InterestRateChartWriteService.class)
    public InterestRateChartWriteService interestRateChartWriteService(InterestRateChartRepositoryWrapper interestRateChartRepository) {
        return new InterestRateChartWriteServiceImpl(interestRateChartRepository);
    }

}
