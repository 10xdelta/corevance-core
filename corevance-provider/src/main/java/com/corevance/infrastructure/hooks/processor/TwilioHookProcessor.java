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
package com.corevance.infrastructure.hooks.processor;

import static com.corevance.commands.domain.CommandWrapperConstants.ACTION_SEND;
import static com.corevance.commands.domain.CommandWrapperConstants.ENTITY_SMS;
import static com.corevance.infrastructure.hooks.api.HookApiConstants.apiKeyName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.domain.CorevanceContext;
import com.corevance.infrastructure.hooks.data.HookSmsProviderData;
import com.corevance.infrastructure.hooks.domain.Hook;
import com.corevance.infrastructure.hooks.domain.HookConfiguration;
import com.corevance.infrastructure.hooks.domain.HookConfigurationRepository;
import com.corevance.portfolio.client.domain.Client;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.template.mapper.TemplateMapper;
import com.corevance.template.service.TemplateMergeService;
import org.springframework.stereotype.Service;
import retrofit2.Callback;

@Service
@RequiredArgsConstructor
public class TwilioHookProcessor implements HookProcessor {

    private final HookConfigurationRepository hookConfigurationRepository;
    private final TemplateMergeService templateMergeService;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ProcessorHelper processorHelper;
    private final TemplateMapper templateMapper;

    @Override
    public void process(final Hook hook, final String payload, final String entityName, final String actionName,
            final CorevanceContext context) throws IOException {

        final HookSmsProviderData smsProviderData = new HookSmsProviderData(hook.getConfig());

        sendRequest(smsProviderData, payload, entityName, actionName, hook, context);
    }

    @SuppressWarnings("unchecked")
    private void sendRequest(final HookSmsProviderData smsProviderData, final String payload, String entityName, String actionName,
            final Hook hook, final CorevanceContext context) throws IOException {

        final WebHookService service = processorHelper.createWebHookService(smsProviderData.getUrl());

        @SuppressWarnings("rawtypes")
        final Callback callback = processorHelper.createCallback(smsProviderData.getUrl());

        String apiKey = this.hookConfigurationRepository.findOneByHookIdAndFieldName(hook.getId(), apiKeyName);
        if (apiKey == null) {
            smsProviderData.setUrl(null);
            smsProviderData.setEndpoint(System.getProperty("baseUrl"));
            smsProviderData.setTenantId(context.getTenantContext().getTenantIdentifier());
            smsProviderData.setMifosToken(context.getAuthTokenContext());
            apiKey = service.sendSmsBridgeConfigRequest(smsProviderData).execute().body();
            final HookConfiguration apiKeyEntry = HookConfiguration.createNew(hook, "string", apiKeyName, apiKey);
            this.hookConfigurationRepository.save(apiKeyEntry);
        }

        if (apiKey != null && !apiKey.equals("")) {
            JsonObject json;
            if (hook.getUgdTemplate() != null) {
                entityName = ENTITY_SMS;
                actionName = ACTION_SEND;
                json = processUgdTemplate(payload, hook);
                if (json == null) {
                    return;
                }
            } else {
                json = JsonParser.parseString(payload).getAsJsonObject();
            }
            service.sendSmsBridgeRequest(entityName, actionName, context.getTenantContext().getTenantIdentifier(), apiKey, json)
                    .enqueue(callback);
        }
    }

    private JsonObject processUgdTemplate(final String payload, final Hook hook) throws IOException {
        JsonObject json = null;
        @SuppressWarnings("unchecked")
        final HashMap<String, Object> map = new ObjectMapper().readValue(payload, HashMap.class);
        map.put("BASE_URI", System.getProperty("baseUrl"));
        if (map.containsKey("clientId")) {
            final Long clientId = Long.valueOf(Integer.toString((int) map.get("clientId")));
            final Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(clientId);
            final String mobileNo = client.mobileNo();
            if (mobileNo != null && !mobileNo.isEmpty()) {
                final String compiledMessage = this.templateMergeService.compile(templateMapper.map(hook.getUgdTemplate()), map)
                        .replace("<p>", "").replace("</p>", "");
                final Map<String, String> jsonMap = new HashMap<>();
                jsonMap.put("mobileNo", mobileNo);
                jsonMap.put("message", compiledMessage);
                final String jsonString = new Gson().toJson(jsonMap);
                json = JsonParser.parseString(jsonString).getAsJsonObject();
            }
        }
        return json;
    }

}
