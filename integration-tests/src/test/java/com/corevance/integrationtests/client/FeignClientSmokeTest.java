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
package com.corevance.integrationtests.client;

import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.feign.CorevanceFeignClientConfig;
import com.corevance.integrationtests.ConfigProperties;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Simple smoke tests for Feign-based Corevance client
 *
 */
public class FeignClientSmokeTest extends FeignIntegrationTest {

    private String getTestUrl() {
        return System.getProperty("corevance.it.url", ConfigProperties.Backend.PROTOCOL + "://" + ConfigProperties.Backend.HOST + ":"
                + ConfigProperties.Backend.PORT + "/corevance-provider/api");
    }

    @Test
    @Order(1)
    public void testFeignClientBuilder() {
        CorevanceFeignClient.Builder builder = CorevanceFeignClient.builder().baseUrl(getTestUrl())
                .credentials(ConfigProperties.Backend.USERNAME, ConfigProperties.Backend.PASSWORD);

        assertThat(builder).isNotNull();
    }

    @Test
    @Order(2)
    public void testFeignClientConfig() {
        CorevanceFeignClientConfig.Builder configBuilder = CorevanceFeignClientConfig.builder().baseUrl(getTestUrl())
                .credentials(ConfigProperties.Backend.USERNAME, ConfigProperties.Backend.PASSWORD).debugEnabled(true);

        CorevanceFeignClientConfig config = configBuilder.build();

        assertThat(config).isNotNull();
    }

    @Test
    @Order(3)
    public void testFeignClientCanInstantiateApis() {
        assertThat(corevanceClient()).isNotNull();
        assertThat(corevanceClient().offices()).isNotNull();
        assertThat(corevanceClient().clients()).isNotNull();
    }
}
