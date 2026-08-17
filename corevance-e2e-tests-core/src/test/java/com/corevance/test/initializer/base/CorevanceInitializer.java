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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.test.initializer.global.CorevanceGlobalInitializerStep;
import com.corevance.test.initializer.scenario.CorevanceScenarioInitializerStep;
import com.corevance.test.initializer.suite.CorevanceSuiteInitializerStep;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
@RequiredArgsConstructor
public class CorevanceInitializer implements InitializingBean {

    public static final String DATE_FORMAT = "dd MMMM yyyy";

    private final List<CorevanceGlobalInitializerStep> globalInitializerSteps;
    private final List<CorevanceSuiteInitializerStep> suiteInitializerSteps;
    private final List<CorevanceScenarioInitializerStep> scenarioInitializerSteps;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.debug("=== CorevanceInitializer.afterPropertiesSet() called ===");
        log.debug("Global initializers count: {}", globalInitializerSteps.size());
        log.debug("Suite initializers count: {}", suiteInitializerSteps.size());
        log.debug("Scenario initializers count: {}", scenarioInitializerSteps.size());

        if (log.isDebugEnabled()) {
            String globalInitializers = globalInitializerSteps.stream().map(Object::getClass).map(Class::getName)
                    .collect(Collectors.joining(", "));
            String suiteInitializers = suiteInitializerSteps.stream().map(Object::getClass).map(Class::getName)
                    .collect(Collectors.joining(", "));
            String scenarioInitializers = scenarioInitializerSteps.stream().map(Object::getClass).map(Class::getName)
                    .collect(Collectors.joining(", "));
            log.debug("""
                    The following initializers have been configured:
                    Global initializers: [{}]
                    Suite initializers: [{}]
                    Scenario initializers: [{}]
                    """, globalInitializers, suiteInitializers, scenarioInitializers);
        } else {
            // Always log the suite initializers at INFO since this is critical
            String suiteInitializers = suiteInitializerSteps.stream().map(Object::getClass).map(Class::getName)
                    .collect(Collectors.joining(", "));
            log.debug("Suite initializers: [{}]", suiteInitializers);
        }
    }

    public void setupGlobalDefaults() throws Exception {
        for (CorevanceGlobalInitializerStep initializerStep : globalInitializerSteps) {
            initializerStep.initialize();
        }
    }

    public void setupDefaultsForSuite() throws Exception {
        log.debug("=== setupDefaultsForSuite() called - {} suite initializers to execute ===", suiteInitializerSteps.size());
        for (CorevanceSuiteInitializerStep initializerStep : suiteInitializerSteps) {
            log.debug("Executing suite initializer: {}", initializerStep.getClass().getName());
            initializerStep.initializeForSuite();
        }
    }

    public void setupDefaultsForScenario() throws Exception {
        for (CorevanceScenarioInitializerStep scenarioInitializerStep : scenarioInitializerSteps) {
            scenarioInitializerStep.initializeForScenario();
        }
    }

    public void resetDefaultsAfterSuite() throws Exception {
        for (CorevanceSuiteInitializerStep initializerStep : suiteInitializerSteps) {
            initializerStep.resetAfterSuite();
        }
    }

    public void resetDefaultsAfterScenario() {
        for (CorevanceScenarioInitializerStep initializerStep : scenarioInitializerSteps) {
            initializerStep.resetAfterScenario();
        }
    }
}
