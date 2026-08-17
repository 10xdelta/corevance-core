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
package com.corevance.test.api;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.feign.CorevanceFeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class CorevanceClientConfiguration {

    private final ApiProperties apiProperties;

    @Bean
    public CorevanceFeignClient corevanceFeignClient() {
        String baseUrl = apiProperties.getBaseUrl();
        String username = apiProperties.getUsername();
        String password = apiProperties.getPassword();
        String tenantId = apiProperties.getTenantId();
        long readTimeout = apiProperties.getReadTimeout();
        String apiBaseUrl = baseUrl + "/corevance-provider/api/";
        boolean debugEnabled = Boolean.parseBoolean(System.getProperty("corevance.feign.debug", "false"));

        return CorevanceFeignClient.builder().baseUrl(apiBaseUrl).credentials(username, password).tenantId(tenantId)
                .disableSslVerification(true).debug(debugEnabled).connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout((int) readTimeout, TimeUnit.SECONDS).build();
    }

    public CorevanceFeignClient corevanceFeignClientForUser(final String username, final String password) {
        final String apiBaseUrl = apiProperties.getBaseUrl() + "/corevance-provider/api/";
        return CorevanceFeignClient.builder().baseUrl(apiBaseUrl).credentials(username, password).tenantId(apiProperties.getTenantId())
                .disableSslVerification(true).connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout((int) apiProperties.getReadTimeout(), TimeUnit.SECONDS).build();
    }
}
