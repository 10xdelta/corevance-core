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
package com.corevance.infrastructure.jobs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.core.domain.ActionContext;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenant;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import com.corevance.infrastructure.core.service.tenant.TenantDetailsService;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SchedulerTriggerListener implements TriggerListener {

    private final TenantDetailsService tenantDetailsService;
    private final SchedulerVetoer schedulerVetoer;

    @Override
    public String getName() {
        return "Corevance Global Scheduler Trigger Listener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        log.debug("triggerFired() trigger={}, context={}", trigger, context);
    }

    @Override
    public boolean vetoJobExecution(final Trigger trigger, final JobExecutionContext context) {
        final String tenantIdentifier = trigger.getJobDataMap().getString(SchedulerServiceConstants.TENANT_IDENTIFIER);
        final CorevancePlatformTenant existingTenant = ThreadLocalContextUtil.getTenant();
        boolean contextInitialized = false;

        try {
            if (existingTenant == null || !existingTenant.getTenantIdentifier().equals(tenantIdentifier)) {
                contextInitialized = true;
                final CorevancePlatformTenant tenant = tenantDetailsService.loadTenantById(tenantIdentifier);
                ThreadLocalContextUtil.setTenant(tenant);
                ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
            }
            return schedulerVetoer.veto(trigger, context);
        } finally {
            if (contextInitialized) {
                ThreadLocalContextUtil.reset();
            }
        }
    }

    @Override
    public void triggerMisfired(final Trigger trigger) {
        log.error("triggerMisfired() trigger={}", trigger);
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction triggerInstructionCode) {
        log.debug("triggerComplete() trigger={}, context={}, completedExecutionInstruction={}", trigger, context, triggerInstructionCode);
    }
}
