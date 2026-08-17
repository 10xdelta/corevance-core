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
package com.corevance.infrastructure.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfig {

    @Autowired
    private CorevanceProperties corevanceProperties;

    @Bean(TaskExecutorConstant.DEFAULT_TASK_EXECUTOR_BEAN_NAME)
    public ThreadPoolTaskExecutor corevanceDefaultThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corevanceProperties.getTaskExecutor().getDefaultTaskExecutorCorePoolSize());
        threadPoolTaskExecutor.setMaxPoolSize(corevanceProperties.getTaskExecutor().getDefaultTaskExecutorMaxPoolSize());
        return threadPoolTaskExecutor;
    }

    @Bean(TaskExecutorConstant.CONFIGURABLE_TASK_EXECUTOR_BEAN_NAME)
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ThreadPoolTaskExecutor corevanceConfigurableThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(corevanceProperties.getTaskExecutor().getDefaultTaskExecutorCorePoolSize());
        threadPoolTaskExecutor.setMaxPoolSize(corevanceProperties.getTaskExecutor().getDefaultTaskExecutorMaxPoolSize());
        return threadPoolTaskExecutor;
    }
}
