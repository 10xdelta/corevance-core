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
package com.corevance.infrastructure.event.external.config;

import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class EventTaskExecutorConfig {

    private final CorevanceProperties corevanceProperties;

    @Bean(TaskExecutorConstant.EVENT_MARKS_AS_SENT_EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor sendAsynchronousEventsThreadPool() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corevanceProperties.getEvents().getExternal().getThreadPoolCorePoolSize());
        threadPoolTaskExecutor.setMaxPoolSize(corevanceProperties.getEvents().getExternal().getThreadPoolMaxPoolSize());
        threadPoolTaskExecutor.setQueueCapacity(corevanceProperties.getEvents().getExternal().getThreadPoolQueueCapacity());
        threadPoolTaskExecutor.setThreadNamePrefix("external-events-");
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}
