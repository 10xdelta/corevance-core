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
package com.corevance.infrastructure;

import static com.corevance.infrastructure.contentstore.processor.ContentProcessor.BEAN_NAME_EXECUTOR;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableConfigurationProperties({ CorevanceProperties.class })
@PropertySource("classpath:application-test.properties")
@ComponentScan({ "com.corevance.infrastructure.contentstore.util", "com.corevance.infrastructure.contentstore.detector",
        "com.corevance.infrastructure.contentstore.processor", "com.corevance.infrastructure.contentstore.policy",
        "com.corevance.infrastructure.contentstore.service" })
public class TestConfiguration {

    @Bean(BEAN_NAME_EXECUTOR)
    public ExecutorService contentProcessorExecutor() {
        return Executors.newCachedThreadPool();
    }
}
