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
package com.corevance.portfolio.rate.handler;

import lombok.RequiredArgsConstructor;
import com.corevance.commands.annotation.CommandType;
import com.corevance.commands.handler.NewCommandSourceHandler;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.portfolio.rate.service.RateWriteService;
import org.springframework.stereotype.Service;

/**
 * Bowpi GT Created by Jose on 19/07/2017.
 */
@Service
@CommandType(entity = "RATE", action = "CREATE")
@RequiredArgsConstructor
public class CreateRateCommandHandler implements NewCommandSourceHandler {

    private final RateWriteService writePlatformService;

    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {
        return this.writePlatformService.createRate(command);
    }
}
