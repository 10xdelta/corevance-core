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
package com.corevance.infrastructure.jobs.service.retainedearning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenant;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import com.corevance.infrastructure.core.service.migration.TenantDataSourceFactory;
import com.corevance.infrastructure.jobs.service.JobName;
import com.corevance.infrastructure.jobs.service.retainedearning.listener.RetainedEarningJobListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;

@ExtendWith(MockitoExtension.class)
class RetainedEarningJobConfigurationTest {

    @Mock
    private RetainedEarningJobListener retainedEarningJobListener;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private RetainedEarningJobWriter retainedEarningJobWriter;

    @Mock
    private RetainedEarningJobReader retainedEarningJobReader;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private CorevanceProperties corevanceProperties;

    @Mock
    private TenantDataSourceFactory tenantDataSourceFactory;

    @Mock
    private CorevanceProperties.CorevanceJobProperties jobProperties;

    @Mock
    private Step step;

    @Mock
    private FlowBuilder flowBuilder;

    @Mock
    private Flow flow;

    @InjectMocks
    private RetainedEarningJobConfiguration retainedEarningJobConfiguration;

    @BeforeEach
    public void setup() {
        ThreadLocalContextUtil.setTenant(new CorevancePlatformTenant(1L, "default", "Default tenant", "UTC", null));
        // Mock CorevanceProperties
        when(corevanceProperties.getJob()).thenReturn(jobProperties);
        when(jobProperties.getRetainedEarningChunkSize()).thenReturn(100);
    }

    @Test
    public void testRetainedEarningSummaryStep() {
        // Execute
        Step result = retainedEarningJobConfiguration.retainedEarningSummaryStep();

        // Verify - basic validation that the step configuration is applied
        assertNotNull(result);
    }

    @Test
    public void testRetainedEarning() {
        // Execute
        Job result = retainedEarningJobConfiguration.retainedEarning();
        // Verify
        assertNotNull(result);
        assertEquals(JobName.RETAINED_EARNING.name(), result.getName());
    }
}
