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
package com.corevance.interoperation.api;

import static com.corevance.interoperation.util.InteropUtil.ENTITY_NAME_IDENTIFIER;
import static com.corevance.interoperation.util.InteropUtil.ENTITY_NAME_QUOTE;
import static com.corevance.interoperation.util.InteropUtil.ENTITY_NAME_REQUEST;
import static com.corevance.interoperation.util.InteropUtil.ENTITY_NAME_TRANSFER;
import static com.corevance.interoperation.util.InteropUtil.ROOT_PATH;

import com.corevance.commands.domain.CommandWrapper;
import com.corevance.interoperation.domain.InteropIdentifierType;
import com.corevance.interoperation.domain.InteropTransferActionType;

public class InteropWrapperBuilder {

    private String actionName;
    private String entityName;
    private String href;
    private String json = "{}";

    public CommandWrapper build() {
        return new CommandWrapper(null, null, null, null, null, actionName, entityName, null, null, href, json, null, null, null, null,
                null, null, null, null, null);
    }

    public InteropWrapperBuilder withJson(final String json) {
        this.json = json;
        return this;
    }

    public InteropWrapperBuilder registerAccountIdentifier(InteropIdentifierType idType, String idValue, String subIdOrType) {
        this.actionName = "CREATE";
        this.entityName = ENTITY_NAME_IDENTIFIER;
        this.href = "/" + ROOT_PATH + "/parties/" + idType + "/" + idValue + "/" + (subIdOrType == null ? " " : subIdOrType);
        return this;
    }

    public InteropWrapperBuilder deleteAccountIdentifier(InteropIdentifierType idType, String idValue, String subIdOrType) {
        this.actionName = "DELETE";
        this.entityName = ENTITY_NAME_IDENTIFIER;
        this.href = "/" + ROOT_PATH + "/parties/" + idType + "/" + idValue + "/" + (subIdOrType == null ? " " : subIdOrType);
        return this;
    }

    public InteropWrapperBuilder createTransactionRequest() {
        this.actionName = "CREATE";
        this.entityName = ENTITY_NAME_REQUEST;
        this.href = "/" + ROOT_PATH + "/requests";
        return this;
    }

    public InteropWrapperBuilder createQuotes() {
        this.actionName = "CREATE";
        this.entityName = ENTITY_NAME_QUOTE;
        this.href = "/" + ROOT_PATH + "/quotes";
        return this;
    }

    public InteropWrapperBuilder performTransfer(InteropTransferActionType action) {
        this.actionName = action.name();
        this.entityName = ENTITY_NAME_TRANSFER;
        this.href = "/" + ROOT_PATH + "/transfers";
        return this;
    }
}
