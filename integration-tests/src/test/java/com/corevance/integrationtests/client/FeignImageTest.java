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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import feign.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.StaffCreateRequest;
import com.corevance.client.models.StaffCreateResponse;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FeignImageTest extends FeignIntegrationTest {

    final File testImage = Path.of(getClass().getResource("/michael.vorburger-crepes.jpg").getFile()).toFile();

    Long staffId;

    @Override
    protected CorevanceFeignClient corevanceClient() {
        return com.corevance.integrationtests.common.CorevanceFeignClientHelper.createNewCorevanceFeignClient("mifos", "password",
                true);
    }

    @Test
    @Order(1)
    void setupStaff() {
        var request = new StaffCreateRequest();
        request.setOfficeId(1L);
        request.setFirstname("Feign");
        request.setLastname("ImageTest" + System.currentTimeMillis());
        request.setJoiningDate(LocalDate.now(ZoneId.of("UTC")).toString());
        request.setDateFormat("yyyy-MM-dd");
        request.setLocale("en_US");

        StaffCreateResponse response = ok(() -> corevanceClient().staff().createStaff(request));
        assertThat(response).isNotNull();
        assertThat(response.getResourceId()).isNotNull();
        staffId = response.getResourceId();
    }

    @Test
    @Order(2)
    void testCreateStaffImage() throws Exception {
        String dataUrl = com.corevance.client.feign.services.ImagesApi.prepareFileUpload(testImage);
        Response response = corevanceClient().images().create("staff", staffId, dataUrl);

        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    @Order(3)
    void testRetrieveStaffImage() throws IOException {
        Response response = corevanceClient().images().get("staff", staffId, new HashMap<>());

        assertNotNull(response);
        assertEquals(200, response.status());

        try (InputStream inputStream = response.body().asInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            assertThat(bytes.length).isGreaterThan(0);
        }
    }

    @Test
    @Order(4)
    void testUpdateStaffImage() {
        Response response = corevanceClient().images().update("staff", staffId,
                com.corevance.client.feign.services.ImagesApi.prepareFileUpload(testImage));

        assertNotNull(response);
        assertEquals(200, response.status());
    }

    @Test
    @Order(99)
    void testDeleteStaffImage() {
        Response response = corevanceClient().images().delete("staff", staffId);

        assertNotNull(response);
        assertEquals(200, response.status());
    }
}
