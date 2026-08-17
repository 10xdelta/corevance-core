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

import java.time.Duration;
import java.util.List;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.feign.services.SchedulerJobApi.RetrieveHistoryQueryParams;
import com.corevance.client.feign.util.FeignCalls;
import com.corevance.client.models.ExecuteJobRequest;
import com.corevance.client.models.GetJobsJobIDJobRunHistoryResponse;
import com.corevance.client.models.GetJobsResponse;
import com.corevance.client.models.JobDetailHistoryDataSwagger;
import org.awaitility.Awaitility;

public class FeignSchedulerHelper {

    private final CorevanceFeignClient corevanceClient;

    public FeignSchedulerHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public void stopScheduler() {
        FeignCalls.executeVoid(() -> corevanceClient.scheduler().handleCommandsScheduler("stop"));
    }

    public void startScheduler() {
        FeignCalls.executeVoid(() -> corevanceClient.scheduler().handleCommandsScheduler("start"));
    }

    public void executeAndAwaitJob(String jobDisplayName) {
        stopScheduler();

        List<GetJobsResponse> allJobs = ok(() -> corevanceClient.schedulerJob().retrieveAllSchedulerJobs());
        GetJobsResponse targetJob = allJobs.stream().filter(j -> jobDisplayName.equals(j.getDisplayName())).findFirst()
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobDisplayName));
        Long jobId = targetJob.getJobId();

        Long previousRunHistoryId = getRunHistoryId(getLatestJobRunHistory(jobId));
        FeignCalls.executeVoid(() -> corevanceClient.schedulerJob().executeJob(jobId, "executeJob", new ExecuteJobRequest()));

        Awaitility.await().atMost(Duration.ofMinutes(2)).pollInterval(Duration.ofSeconds(1)).pollDelay(Duration.ofSeconds(1))
                .until(() -> isNewCompletedRunHistory(jobId, previousRunHistoryId));
    }

    private boolean isNewCompletedRunHistory(Long jobId, Long previousRunHistoryId) {
        JobDetailHistoryDataSwagger latestRunHistory = getLatestJobRunHistory(jobId);
        if (latestRunHistory == null || latestRunHistory.getJobRunEndTime() == null) {
            return false;
        }
        Long runHistoryId = latestRunHistory.getId();
        return runHistoryId != null && (previousRunHistoryId == null || runHistoryId > previousRunHistoryId);
    }

    private Long getRunHistoryId(JobDetailHistoryDataSwagger runHistory) {
        return runHistory == null ? null : runHistory.getId();
    }

    private JobDetailHistoryDataSwagger getLatestJobRunHistory(Long jobId) {
        RetrieveHistoryQueryParams queryParams = new RetrieveHistoryQueryParams().offset(0).limit(1).orderBy("id").sortOrder("DESC");
        GetJobsJobIDJobRunHistoryResponse response = ok(() -> corevanceClient.schedulerJob().retrieveHistory(jobId, queryParams));
        List<JobDetailHistoryDataSwagger> pageItems = response.getPageItems();
        if (pageItems == null || pageItems.isEmpty()) {
            return null;
        }
        return pageItems.get(0);
    }
}
