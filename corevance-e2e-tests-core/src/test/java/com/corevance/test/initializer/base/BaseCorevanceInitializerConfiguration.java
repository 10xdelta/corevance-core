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
package com.corevance.test.initializer.base;

import java.util.List;
import com.corevance.test.config.CacheConfiguration;
import com.corevance.test.config.TestDatabaseConfiguration;
import com.corevance.test.initializer.global.CorevanceGlobalInitializerStep;
import com.corevance.test.initializer.scenario.CorevanceScenarioInitializerStep;
import com.corevance.test.initializer.suite.CorevanceSuiteInitializerStep;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan({ "com.corevance.test.api", "com.corevance.test.helper" })
@PropertySource("classpath:corevance-test-application.properties")
@Import({ CacheConfiguration.class, TestDatabaseConfiguration.class })
public class BaseCorevanceInitializerConfiguration {

    @Bean
    public CorevanceInitializer corevanceInitializer(List<CorevanceGlobalInitializerStep> globalInitializerSteps,
            List<CorevanceSuiteInitializerStep> suiteInitializerSteps, List<CorevanceScenarioInitializerStep> scenarioInitializerSteps) {
        return new CorevanceInitializer(globalInitializerSteps, suiteInitializerSteps, scenarioInitializerSteps);
    }
}
