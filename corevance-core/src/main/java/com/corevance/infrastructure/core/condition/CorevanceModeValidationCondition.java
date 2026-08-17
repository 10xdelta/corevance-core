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
package com.corevance.infrastructure.core.condition;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class CorevanceModeValidationCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean isReadModeEnabled = Optional.ofNullable(context.getEnvironment().getProperty("corevance.mode.read-enabled", Boolean.class))
                .orElse(true);
        boolean isWriteModeEnabled = Optional.ofNullable(context.getEnvironment().getProperty("corevance.mode.write-enabled", Boolean.class))
                .orElse(true);
        boolean isBatchManagerModeEnabled = Optional
                .ofNullable(context.getEnvironment().getProperty("corevance.mode.batch-manager-enabled", Boolean.class)).orElse(true);
        boolean isBatchWorkerModeEnabled = Optional
                .ofNullable(context.getEnvironment().getProperty("corevance.mode.batch-worker-enabled", Boolean.class)).orElse(true);
        boolean isValidationFails = !isReadModeEnabled && !isWriteModeEnabled && !isBatchManagerModeEnabled && !isBatchWorkerModeEnabled;
        if (isValidationFails) {
            log.error(
                    "The Corevance instance type is not configured properly. At least one of these environment variables should be true: COREVANCE_MODE_READ_ENABLED, COREVANCE_MODE_WRITE_ENABLED, COREVANCE_MODE_BATCH_ENABLED");
        }
        return isValidationFails;
    }
}
