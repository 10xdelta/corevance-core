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

import java.util.List;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.core.config.ExplicitConfigurationPropertiesFactory;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class CorevancePartitionJobConfigValidationCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        CorevanceProperties.CorevancePartitionedJob partitionedJobProperties = ExplicitConfigurationPropertiesFactory.getProperty(context,
                "corevance.partitioned-job", CorevanceProperties.CorevancePartitionedJob.class);
        if (partitionedJobProperties != null) {
            List<CorevanceProperties.PartitionedJobProperty> invalidConfigs = partitionedJobProperties.getPartitionedJobProperties().stream()
                    .filter(isAnyConfigBelowOne().or(CorevancePartitionJobConfigValidationCondition::invalidMaxPoolSize)).toList();
            if (!invalidConfigs.isEmpty()) {
                for (CorevanceProperties.PartitionedJobProperty invalidConfig : invalidConfigs) {
                    log.error(
                            "{} partitioned job is not configured properly. The partition size, chunk size and thread count must be more than 0, and partition size must be less then chunk size * thread count",
                            invalidConfig.getJobName());
                }
            }
            return !invalidConfigs.isEmpty();
        } else {
            return false;
        }
    }

    private static Predicate<CorevanceProperties.PartitionedJobProperty> isAnyConfigBelowOne() {
        return partitionedJobProperty -> !(partitionedJobProperty.getPartitionSize() > 0 && partitionedJobProperty.getChunkSize() > 0
                && partitionedJobProperty.getThreadPoolCorePoolSize() > 0 && partitionedJobProperty.getThreadPoolMaxPoolSize() > 0
                && partitionedJobProperty.getThreadPoolQueueCapacity() > 0);
    }

    private static boolean invalidMaxPoolSize(CorevanceProperties.PartitionedJobProperty partitionedJobProperty) {
        return partitionedJobProperty.getThreadPoolMaxPoolSize() < partitionedJobProperty.getThreadPoolCorePoolSize();
    }
}
