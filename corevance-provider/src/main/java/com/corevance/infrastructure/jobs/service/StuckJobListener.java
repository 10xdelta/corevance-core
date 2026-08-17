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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.businessdate.domain.BusinessDateType;
import com.corevance.infrastructure.businessdate.service.BusinessDateReadPlatformService;
import com.corevance.infrastructure.core.domain.ActionContext;
import com.corevance.infrastructure.core.service.JdbcTemplateFactory;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import com.corevance.infrastructure.core.service.tenant.TenantDetailsService;
import com.corevance.infrastructure.jobs.domain.JobExecutionRepository;
import com.corevance.useradministration.domain.AppUser;
import com.corevance.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "corevance.mode.batch-manager-enabled", havingValue = "true")
public class StuckJobListener implements ApplicationListener<ContextRefreshedEvent> {

    private final JobExecutionRepository jobExecutionRepository;
    private final JdbcTemplateFactory jdbcTemplateFactory;
    private final TenantDetailsService tenantDetailsService;
    private final JobRegistry jobRegistry;
    private final BusinessDateReadPlatformService businessDateReadPlatformService;
    private final StuckJobExecutorService stuckJobExecutorService;
    private final AppUserRepositoryWrapper userRepository;

    @Override
    public void onApplicationEvent(@NonNull final ContextRefreshedEvent event) {
        if (jobRegistry.getJobNames().isEmpty()) {
            return;
        }
        tenantDetailsService.findAllTenants().forEach(tenant -> {
            try {
                ThreadLocalContextUtil.setTenant(tenant);
                final NamedParameterJdbcTemplate namedParameterJdbcTemplate = jdbcTemplateFactory.createNamedParameterJdbcTemplate(tenant);
                final List<String> stuckJobNames = jobExecutionRepository.getStuckJobNames(namedParameterJdbcTemplate);
                if (!stuckJobNames.isEmpty()) {
                    try {
                        final HashMap<BusinessDateType, LocalDate> businessDates = businessDateReadPlatformService.getBusinessDates();
                        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
                        ThreadLocalContextUtil.setBusinessDates(businessDates);
                        final AppUser user = userRepository.fetchSystemUser();
                        final UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, user.getPassword(),
                                user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                        stuckJobNames.forEach(stuckJobExecutorService::resumeStuckJob);
                    } finally {
                        SecurityContextHolder.getContext().setAuthentication(null);
                    }
                }
            } finally {
                ThreadLocalContextUtil.reset();
            }
        });
    }
}
