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
package com.corevance.infrastructure.jobs.service.aggregationjob;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.service.database.DatabaseTypeResolver;
import com.corevance.infrastructure.core.service.migration.TenantDataSourceFactory;
import com.corevance.infrastructure.jobs.service.aggregationjob.listener.JournalEntryAggregationJobListener;
import com.corevance.infrastructure.jobs.service.aggregationjob.tasklet.JournalEntryAggregationTrackingTasklet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.JobSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.transaction.PlatformTransactionManager;

@EnableConfigurationProperties({ CorevanceProperties.class })
@ExtendWith(MockitoExtension.class)
class JournalEntryAggregationJobConfigurationTest {

    @InjectMocks
    private JournalEntryAggregationJobConfiguration configuration;
    @Mock
    JournalEntryAggregationJobListener journalEntryAggregationJobListener;
    @Mock
    private JobRepository jobRepository;

    @Mock
    private JournalEntryAggregationJobExecutionDecider journalEntryAggregationJobExecutionDecider;

    @Mock
    private JournalEntryAggregationJobWriter aggregationItemWriter;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private JournalEntryAggregationTrackingTasklet journalEntryAggregationTrackingTasklet;

    @Mock
    private TenantDataSourceFactory tenantDataSourceFactory;

    @Mock
    private DatabaseTypeResolver databaseTypeResolver;

    @Mock
    private CorevanceProperties corevanceProperties;

    @Mock
    private CorevanceProperties.CorevanceJobProperties corevanceJobProperties;

    @Mock
    private CorevanceProperties.CorevanceJournalEntryAggregationProperties journalEntryAggregationProperties;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private ExecutionContext executionContext;

    /**
     * Test method for {@link JournalEntryAggregationJobConfiguration#journalEntryAggregation()}.
     */
    @Test
    public void testJournalEntryDailyAggregationJob() {
        given(corevanceProperties.getJob()).willReturn(corevanceJobProperties);
        given(corevanceJobProperties.getJournalEntryAggregation()).willReturn(journalEntryAggregationProperties);
        given(corevanceJobProperties.getJournalEntryAggregation().getChunkSize()).willReturn(5);
        JobSynchronizationManager.register(jobExecution);
        assertNotNull(configuration.journalEntryAggregation(), "The journalEntryDailyAggregationJob bean should not be null");
    }
}
