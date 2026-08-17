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
package com.corevance.integrationtests.common;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.integrationtests.ConfigProperties;

public final class CorevanceFeignClientHelper {

    private static final int READ_TIMEOUT_SECONDS = 180;

    private static final CorevanceFeignClient DEFAULT_COREVANCE_FEIGN_CLIENT = createNewCorevanceFeignClient(ConfigProperties.Backend.USERNAME,
            ConfigProperties.Backend.PASSWORD);

    private CorevanceFeignClientHelper() {}

    public static CorevanceFeignClient getCorevanceFeignClient() {
        return DEFAULT_COREVANCE_FEIGN_CLIENT;
    }

    public static CorevanceFeignClient createNewCorevanceFeignClient(String username, String password) {
        return createNewCorevanceFeignClient(username, password, Function.identity()::apply);
    }

    public static CorevanceFeignClient createNewCorevanceFeignClient(String username, String password, boolean debugEnabled) {
        return createNewCorevanceFeignClient(username, password, builder -> builder.debug(debugEnabled));
    }

    public static CorevanceFeignClient createNewCorevanceFeignClient(String username, String password,
            Consumer<CorevanceFeignClient.Builder> customizer) {
        String url = System.getProperty("corevance.it.url", buildURI());
        CorevanceFeignClient.Builder builder = CorevanceFeignClient.builder().baseUrl(url).credentials(username, password)
                .disableSslVerification(true).readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        customizer.accept(builder);
        return builder.build();
    }

    private static String buildURI() {
        return ConfigProperties.Backend.PROTOCOL + "://" + ConfigProperties.Backend.HOST + ":" + ConfigProperties.Backend.PORT
                + "/corevance-provider/api";
    }
}
