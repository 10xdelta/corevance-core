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
package com.corevance.infrastructure.core.exception;

import static com.corevance.infrastructure.core.exception.AbstractIdempotentCommandException.IDEMPOTENT_CACHE_HEADER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import com.corevance.commands.domain.CommandSource;
import com.corevance.commands.domain.CommandWrapper;
import com.corevance.infrastructure.businessdate.domain.BusinessDateType;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.domain.ActionContext;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenant;
import com.corevance.infrastructure.core.exceptionmapper.IdempotentCommandExceptionMapper;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class IdempotencyCommandProcessFailedExceptionTest {

    private final LocalDate actualDate = LocalDate.now(ZoneId.systemDefault());

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new CorevancePlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, actualDate)));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testInconsistentStatus() {

        IdempotentCommandExceptionMapper mapper = new IdempotentCommandExceptionMapper();
        CommandWrapper command = new CommandWrapper(null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
        CommandSource source = CommandSource.fullEntryFrom(command, JsonCommand.from("{}"), null, "dummy-key", null, false);
        IdempotentCommandProcessFailedException exception = new IdempotentCommandProcessFailedException(command, null, source);
        Response result = mapper.toResponse(exception);
        assertEquals(500, result.getStatus());
        assertEquals("true", result.getHeaderString(IDEMPOTENT_CACHE_HEADER));

    }
}
