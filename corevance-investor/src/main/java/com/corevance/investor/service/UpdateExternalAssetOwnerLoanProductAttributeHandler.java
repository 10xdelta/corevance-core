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
package com.corevance.investor.service;

import com.google.gson.JsonElement;
import lombok.RequiredArgsConstructor;
import com.corevance.commands.annotation.CommandType;
import com.corevance.commands.handler.NewCommandSourceHandler;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.investor.data.ExternalAssetOwnerLoanProductAttributeRequestParameters;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@CommandType(entity = "EXTERNAL_ASSET_OWNER_LOAN_PRODUCT_ATTRIBUTE", action = "UPDATE")
public class UpdateExternalAssetOwnerLoanProductAttributeHandler implements NewCommandSourceHandler {

    private final FromJsonHelper fromApiJsonHelper;
    private final ExternalAssetOwnerLoanProductAttributesWriteService externalAssetOwnerLoanProductAttributesWriteService;

    @Override
    public CommandProcessingResult processCommand(JsonCommand command) {
        final JsonElement json = fromApiJsonHelper.parse(command.json());
        String attributeKey = fromApiJsonHelper.extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_KEY,
                json);
        String attributeValue = fromApiJsonHelper
                .extractStringNamed(ExternalAssetOwnerLoanProductAttributeRequestParameters.ATTRIBUTE_VALUE, json);
        return externalAssetOwnerLoanProductAttributesWriteService.updateExternalAssetOwnerLoanProductAttribute(command, attributeKey,
                attributeValue);
    }
}
