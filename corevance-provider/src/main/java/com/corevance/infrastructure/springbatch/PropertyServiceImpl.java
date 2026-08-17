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
package com.corevance.infrastructure.springbatch;

import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final CorevanceProperties corevanceProperties;

    @Override
    public Integer getPartitionSize(String jobName) {
        return getProperty(jobName, CorevanceProperties.PartitionedJobProperty::getPartitionSize);
    }

    @Override
    public Integer getChunkSize(String jobName) {
        return getProperty(jobName, CorevanceProperties.PartitionedJobProperty::getChunkSize);
    }

    @Override
    public Integer getRetryLimit(String jobName) {
        return getProperty(jobName, CorevanceProperties.PartitionedJobProperty::getRetryLimit);
    }

    @Override
    public Integer getThreadPoolCorePoolSize(String jobName) {
        return getProperty(jobName, CorevanceProperties.PartitionedJobProperty::getThreadPoolCorePoolSize);
    }

    @Override
    public Integer getThreadPoolMaxPoolSize(String jobName) {
        return getProperty(jobName, CorevanceProperties.PartitionedJobProperty::getThreadPoolMaxPoolSize);
    }

    @Override
    public Integer getThreadPoolQueueCapacity(String jobName) {
        return getProperty(jobName, CorevanceProperties.PartitionedJobProperty::getThreadPoolQueueCapacity);
    }

    @Override
    public Integer getPollInterval(String jobName) {
        return getProperty(jobName, CorevanceProperties.PartitionedJobProperty::getPollInterval);
    }

    private Integer getProperty(String jobName, Function<? super CorevanceProperties.PartitionedJobProperty, Integer> function) {
        List<CorevanceProperties.PartitionedJobProperty> jobProperties = corevanceProperties.getPartitionedJob()
                .getPartitionedJobProperties();
        return jobProperties.stream() //
                .filter(jobProperty -> jobName.equals(jobProperty.getJobName())) //
                .findFirst() //
                .map(function) //
                .orElse(1);
    }
}
