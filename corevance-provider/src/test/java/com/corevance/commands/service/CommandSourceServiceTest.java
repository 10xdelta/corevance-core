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
package com.corevance.commands.service;

import static com.corevance.commands.domain.CommandProcessingResultType.UNDER_PROCESSING;
import static org.mockito.ArgumentMatchers.any;

import java.time.ZoneId;
import java.util.Optional;
import com.corevance.batch.exception.ErrorInfo;
import com.corevance.commands.domain.CommandSource;
import com.corevance.commands.domain.CommandSourceRepository;
import com.corevance.commands.domain.CommandWrapper;
import com.corevance.infrastructure.codes.exception.CodeNotFoundException;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenant;
import com.corevance.infrastructure.core.exception.ErrorHandler;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import com.corevance.useradministration.domain.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CommandSourceServiceTest {

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private CommandSourceRepository commandSourceRepository;

    @Mock
    private ErrorHandler errorHandler;

    @InjectMocks
    private CommandSourceService underTest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testCreateFromWrapper() {
        CommandWrapper wrapper = CommandWrapper.wrap("act", "ent", 1L, 1L);
        JsonCommand jsonCommand = JsonCommand.from("{}");
        AppUser appUser = Mockito.mock(AppUser.class);

        CorevancePlatformTenant ft = new CorevancePlatformTenant(1L, "t1", "n1", ZoneId.systemDefault().toString(), null);
        ThreadLocalContextUtil.setTenant(ft);

        String idk = "idk";
        underTest.saveInitial(wrapper, jsonCommand, appUser, idk);

        ArgumentCaptor<CommandSource> commandSourceArgumentCaptor = ArgumentCaptor.forClass(CommandSource.class);
        Mockito.verify(commandSourceRepository).saveAndFlush(commandSourceArgumentCaptor.capture());

        CommandSource captured = commandSourceArgumentCaptor.getValue();
        Assertions.assertEquals(idk, captured.getIdempotencyKey());
        Assertions.assertEquals(UNDER_PROCESSING.getValue(), captured.getStatus());
    }

    @Test
    public void testCreateFromExisting() {
        long commandId = 1L;
        CommandSource commandMock = Mockito.mock(CommandSource.class);
        Mockito.when(commandSourceRepository.findById(commandId)).thenReturn(Optional.of(commandMock));

        CommandSource actual = underTest.getCommandSource(commandId);
        Assertions.assertEquals(commandMock, actual);
    }

    @Test
    public void testGenerateErrorException() {
        try (MockedStatic<ErrorHandler> mockedStatic = Mockito.mockStatic(ErrorHandler.class)) {
            mockedStatic.when(() -> ErrorHandler.getMappable(any(CodeNotFoundException.class))).thenAnswer(i -> i.getArguments()[0]);
        }
        Mockito.when(errorHandler.handle(any(CodeNotFoundException.class)))
                .thenReturn(new ErrorInfo(404, 1001, "Code with name `foo` does not exist", null));
        ErrorInfo result = underTest.generateErrorInfo(new CodeNotFoundException("foo"));
        Assertions.assertEquals(404, result.getStatusCode());
        Assertions.assertEquals(1001, result.getErrorCode());
        Assertions.assertTrue(result.getMessage().contains("Code with name `foo` does not exist"));
    }
}
