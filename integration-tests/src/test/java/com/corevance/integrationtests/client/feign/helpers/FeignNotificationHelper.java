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
package com.corevance.integrationtests.client.feign.helpers;

import static com.corevance.client.feign.util.FeignCalls.ok;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.GetNotificationsResponse;

public class FeignNotificationHelper {

    private final CorevanceFeignClient corevanceClient;

    public FeignNotificationHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public GetNotificationsResponse getNotifications() {
        return ok(() -> corevanceClient.notification().getAllNotifications(null, null, null, null, true));
    }

    public GetNotificationsResponse getNotifications(Boolean isRead) {
        return ok(() -> corevanceClient.notification().getAllNotifications(null, null, null, null, isRead));
    }

    public GetNotificationsResponse getUnreadNotifications() {
        return ok(() -> corevanceClient.notification().getAllNotifications(null, null, null, null, false));
    }

    public void markNotificationsAsRead() {
        corevanceClient.notification().updateNotificationReadStatus();
    }

    public boolean areNotificationsAvailable() {
        return !getNotifications().getPageItems().isEmpty();
    }

    public void waitUntilNotificationsAreAvailable() {
        await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(5))
                .until(this::areNotificationsAvailable);
    }
}
