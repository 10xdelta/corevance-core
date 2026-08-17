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
package com.corevance.mix.handler;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.command.core.Command;
import com.corevance.command.core.CommandHandler;
import com.corevance.mix.data.MixTaxonomyMappingUpdateRequest;
import com.corevance.mix.data.MixTaxonomyMappingUpdateResponse;
import com.corevance.mix.service.MixTaxonomyMappingWriteService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MixTaxonomyMappingUpdateCommandHandler
        implements CommandHandler<MixTaxonomyMappingUpdateRequest, MixTaxonomyMappingUpdateResponse> {

    private final MixTaxonomyMappingWriteService writeTaxonomyService;

    @Retry(name = "commandMixTaxonomyMappingUpdate", fallbackMethod = "fallback")
    @Transactional
    @Override
    public MixTaxonomyMappingUpdateResponse handle(Command<MixTaxonomyMappingUpdateRequest> command) {
        return writeTaxonomyService.updateMapping(command.getPayload());
    }

    @Override
    public MixTaxonomyMappingUpdateResponse fallback(Command<MixTaxonomyMappingUpdateRequest> command, Throwable t) {
        return CommandHandler.super.fallback(command, t);
    }
}
