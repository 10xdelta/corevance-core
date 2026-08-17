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
package com.corevance.test.initializer.global;

import static com.corevance.client.feign.util.FeignCalls.executeVoid;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.corevance.client.feign.CorevanceFeignClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SchedulerGlobalInitializerStep implements CorevanceGlobalInitializerStep {

    private static final String SCHEDULER_STATUS_STOP = "stop";

    private final CorevanceFeignClient corevanceClient;

    @Override
    public void initialize() throws Exception {
        executeVoid(() -> corevanceClient.scheduler().handleCommandsScheduler(Map.of("command", SCHEDULER_STATUS_STOP)));
    }
}
