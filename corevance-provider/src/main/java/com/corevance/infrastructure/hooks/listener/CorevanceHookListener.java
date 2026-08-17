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
package com.corevance.infrastructure.hooks.listener;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.core.domain.CorevanceContext;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import com.corevance.infrastructure.hooks.domain.Hook;
import com.corevance.infrastructure.hooks.event.HookEvent;
import com.corevance.infrastructure.hooks.event.HookEventSource;
import com.corevance.infrastructure.hooks.processor.HookProcessor;
import com.corevance.infrastructure.hooks.processor.HookProcessorProvider;
import com.corevance.infrastructure.hooks.service.HookReadPlatformService;
import com.corevance.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorevanceHookListener implements HookListener {

    private final HookProcessorProvider hookProcessorProvider;
    private final HookReadPlatformService hookReadPlatformService;

    @Override
    public void onApplicationEvent(final HookEvent event) {
        try {
            ThreadLocalContextUtil.init(event.getContext());

            final AppUser appUser = event.getAppUser();

            final HookEventSource hookEventSource = (HookEventSource) event.getSource();
            final CorevanceContext corevanceContext = event.getContext();
            final String entityName = hookEventSource.getEntityName();
            final String actionName = hookEventSource.getActionName();
            final String payload = event.getPayload();

            final List<Hook> hooks = hookReadPlatformService.retrieveHooksByEvent(hookEventSource.getEntityName(),
                    hookEventSource.getActionName());

            for (final Hook hook : hooks) {
                final HookProcessor processor = hookProcessorProvider.getProcessor(hook);
                try {
                    processor.process(hook, payload, entityName, actionName, corevanceContext);
                } catch (Throwable e) {
                    log.error(
                            "Hook {} failed in HookProcessor {} for tenantIdentifier/user {}/{}, entityName: {}, actionName: {}, payload {} ",
                            hook.getId(), processor.getClass().getSimpleName(), corevanceContext.getTenantContext().getTenantIdentifier(),
                            appUser.getDisplayName(), entityName, actionName, payload, e);
                }
            }
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }
}
