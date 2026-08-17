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
package com.corevance.client.test;

import java.util.List;
import com.corevance.client.models.StaffData;
import com.corevance.client.util.Calls;
import com.corevance.client.util.CorevanceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo code which is included in the corevance-doc/src/docs/en/05_client.adoc.
 *
 * This is not a real running integration test - those are in
 * integration-tests/src/test/java/com/corevance/integrationtests/client.
 *
 * @author Michael Vorburger.ch
 */
public class CorevanceClientDemo {

    private static final Logger log = LoggerFactory.getLogger(CorevanceClientDemo.class);

    void demoClient() {
        // tag::documentation[]
        CorevanceClient corevance = CorevanceClient.builder().baseURL("https://demo.corevance.dev/corevance-provider/api/v1/").tenant("default")
                .basicAuth("mifos", "password").build();
        List<StaffData> staff = Calls.ok(corevance.staff.retrieveAllStaff(1L, true, false, "ACTIVE"));
        String name = staff.get(0).getDisplayName();
        log.info("Display name: {}", name);
        // end::documentation[]
    }

}
