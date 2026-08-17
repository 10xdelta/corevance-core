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
package com.corevance.infrastructure.configuration.serialization;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.corevance.infrastructure.configuration.command.UpdateGlobalConfigurationCommand;
import com.corevance.infrastructure.core.exception.InvalidJsonException;
import com.corevance.infrastructure.core.serialization.AbstractFromApiJsonDeserializer;
import com.corevance.infrastructure.core.serialization.FromApiJsonDeserializer;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link FromApiJsonDeserializer} for {@link UpdateGlobalConfigurationCommand}'s.
 */
@Component
public final class GlobalConfigurationCommandFromApiJsonDeserializer
        extends AbstractFromApiJsonDeserializer<UpdateGlobalConfigurationCommand> {

    /**
     * The parameters supported for this command.
     */
    private static final Set<String> SUPPORTED_PARAMETERS = new HashSet<>(List.of("globalConfiguration"));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public GlobalConfigurationCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @Override
    public UpdateGlobalConfigurationCommand commandFromApiJson(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SUPPORTED_PARAMETERS);

        return this.fromApiJsonHelper.fromJson(json, UpdateGlobalConfigurationCommand.class);
    }
}
