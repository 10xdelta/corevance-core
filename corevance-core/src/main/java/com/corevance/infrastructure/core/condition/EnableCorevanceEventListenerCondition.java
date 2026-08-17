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
import com.corevance.infrastructure.instancemode.api.CorevanceInstanceModeConstants;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EnableCorevanceEventListenerCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean isReadModeEnabled = Optional.ofNullable(
                context.getEnvironment().getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_READ_ENABLE_PROPERTY, Boolean.class))
                .orElse(true);
        boolean isWriteModeEnabled = Optional.ofNullable(
                context.getEnvironment().getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_WRITE_ENABLE_PROPERTY, Boolean.class))
                .orElse(true);
        boolean isBatchModeEnabled = Optional.ofNullable(
                context.getEnvironment().getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_BATCH_ENABLE_PROPERTY, Boolean.class))
                .orElse(true);
        return (isReadModeEnabled && isBatchModeEnabled && isWriteModeEnabled) // All mode
                || (!isReadModeEnabled && !isBatchModeEnabled && isWriteModeEnabled); // Write mode
    }
}
