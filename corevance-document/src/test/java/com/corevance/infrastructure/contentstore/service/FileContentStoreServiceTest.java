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
package com.corevance.infrastructure.contentstore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.TestConfiguration;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenant;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
class FileContentStoreServiceTest {

    @Autowired
    private FileContentStoreService fileContentStoreService;

    @Autowired
    private CorevanceProperties properties;

    @Test
    void upload() {
        setTenant();

        var path = fileContentStoreService.upload("some/path/test.jpg",
                FileContentStoreServiceTest.class.getClassLoader().getResourceAsStream("test.jpg"), "image/jpeg");

        var rootFolder = getRootFolder();

        log.info("Path: {} ({})", path, rootFolder);

        var p = rootFolder.resolve(path);

        log.info("Complete path: {}", p);

        assertTrue(Files.exists(p));
    }

    @Test
    void relativePath() {
        var rootPath = Path.of("/tmp/corevance/DefaultDemoTenant");
        var fullPath = Path.of("/tmp/corevance/DefaultDemoTenant/images/staff/1/PwVhJDLHyxUppiby/michael.vorburger-crepes.jpg");
        var relativePath = rootPath.relativize(fullPath);

        assertEquals(Path.of("images/staff/1/PwVhJDLHyxUppiby/michael.vorburger-crepes.jpg"), relativePath);

        log.info("Relative path: {}", relativePath);
    }

    private Path getRootFolder() {
        return Path.of(properties.getContent().getFilesystem().getRootFolder(),
                ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim());
    }

    private void setTenant() {
        ThreadLocalContextUtil
                .setTenant(CorevancePlatformTenant.builder().id(1L).name("Test Tenant").tenantIdentifier("test-tenant").build());
    }
}
