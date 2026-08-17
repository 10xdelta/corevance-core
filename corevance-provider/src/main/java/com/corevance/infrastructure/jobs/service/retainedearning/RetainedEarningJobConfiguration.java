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

import static com.corevance.infrastructure.jobs.service.retainedearning.RetainedEarningJobConstant.JOB_SUMMARY_STEP_NAME;

import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.jobs.service.JobName;
import com.corevance.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import com.corevance.infrastructure.jobs.service.retainedearning.listener.RetainedEarningJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuration for Retained earning job
 */
@Configuration
@RequiredArgsConstructor
public class RetainedEarningJobConfiguration {

    private final RetainedEarningJobListener retainedEarningJobListener;
    private final JobRepository jobRepository;
    private final RetainedEarningJobWriter retainedEarningItemWriter;
    private final PlatformTransactionManager transactionManager;
    private final CorevanceProperties corevanceProperties;
    private final RetainedEarningJobReader retainedEarningJobReader;

    /**
     * Step to insert into summary table
     *
     * @return summary insert step
     */
    @Bean
    public Step retainedEarningSummaryStep() {
        return new StepBuilder(JOB_SUMMARY_STEP_NAME, jobRepository)
                .<AccountGLJournalEntryAnnualSummaryData, AccountGLJournalEntryAnnualSummaryData>chunk(
                        corevanceProperties.getJob().getRetainedEarningChunkSize(), transactionManager)
                .reader(retainedEarningJobReader).writer(retainedEarningItemWriter).allowStartIfComplete(true).build();
    }

    /**
     * Retained Earning job with proper data flow between reader, processor, and writer
     *
     * @return {@link Job} configured job with proper step sequence
     */
    @Bean(name = "retainedEarning")
    public Job retainedEarning() {
        return new JobBuilder(JobName.RETAINED_EARNING.name(), jobRepository).listener(retainedEarningJobListener)
                .start(retainedEarningSummaryStep()).incrementer(new RunIdIncrementer()).build();
    }

}
