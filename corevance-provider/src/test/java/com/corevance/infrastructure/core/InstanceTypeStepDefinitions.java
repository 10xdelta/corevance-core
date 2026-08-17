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
package com.corevance.infrastructure.core;

import io.cucumber.java8.En;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import org.springframework.beans.factory.annotation.Autowired;

public class InstanceTypeStepDefinitions implements En {

    @Autowired
    private CorevanceProperties corevanceProperties;

    public InstanceTypeStepDefinitions() {
        Given("Set every Corevance instance type to false", () -> {
            corevanceProperties.getMode().setWriteEnabled(false);
            corevanceProperties.getMode().setReadEnabled(false);
            corevanceProperties.getMode().setBatchWorkerEnabled(false);
            corevanceProperties.getMode().setBatchManagerEnabled(false);
        });
        Given("Corevance instance is a write instance", () -> {
            corevanceProperties.getMode().setWriteEnabled(true);
        });
        Given("Corevance instance is a read instance", () -> {
            corevanceProperties.getMode().setReadEnabled(true);
        });
        Given("Corevance instance is a batch manager instance", () -> {
            corevanceProperties.getMode().setBatchManagerEnabled(true);
        });
    }
}
