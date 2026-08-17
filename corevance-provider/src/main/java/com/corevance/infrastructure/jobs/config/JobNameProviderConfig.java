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
package com.corevance.infrastructure.jobs.config;

import java.util.Arrays;
import java.util.List;
import com.corevance.infrastructure.jobs.service.JobName;
import com.corevance.infrastructure.jobs.service.jobname.JobNameData;
import com.corevance.infrastructure.jobs.service.jobname.JobNameProvider;
import com.corevance.infrastructure.jobs.service.jobname.SimpleJobNameProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobNameProviderConfig {

    @Bean
    public JobNameProvider corevanceJobNameProvider() {
        List<JobNameData> jobNames = Arrays.stream(JobName.values()).map(jn -> new JobNameData(jn.name(), jn.toString())).toList();
        return new SimpleJobNameProvider(jobNames);
    }
}
