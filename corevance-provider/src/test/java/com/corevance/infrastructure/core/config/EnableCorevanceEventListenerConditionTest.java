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
package com.corevance.infrastructure.core.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.corevance.infrastructure.core.condition.EnableCorevanceEventListenerCondition;
import com.corevance.infrastructure.instancemode.api.CorevanceInstanceModeConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EnableCorevanceEventListenerConditionTest {

    @InjectMocks
    private EnableCorevanceEventListenerCondition underTest;

    @Mock
    private AnnotatedTypeMetadata metadata;

    private ConditionContext context;
    private Environment environment;

    @BeforeEach
    void setUp() {
        context = mock(ConditionContext.class);
        environment = mock(Environment.class);
    }

    @Test
    void testOnMessage_ShouldBlockOnMessage_WhenCorevanceIsInBatchMode() {

        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_READ_ENABLE_PROPERTY, Boolean.class)).willReturn(false);
        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_WRITE_ENABLE_PROPERTY, Boolean.class)).willReturn(false);
        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_BATCH_ENABLE_PROPERTY, Boolean.class)).willReturn(true);
        given(context.getEnvironment()).willReturn(environment);

        assertFalse(underTest.matches(context, metadata));
    }

    @Test
    void testOnMessage_ShouldAllowOnMessage_WhenCorevanceIsInAllMode() {

        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_READ_ENABLE_PROPERTY, Boolean.class)).willReturn(true);
        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_WRITE_ENABLE_PROPERTY, Boolean.class)).willReturn(true);
        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_BATCH_ENABLE_PROPERTY, Boolean.class)).willReturn(true);
        given(context.getEnvironment()).willReturn(environment);

        assertTrue(underTest.matches(context, metadata));
    }

    @Test
    void testOnMessage_ShouldAllowOnMessage_WhenCorevanceIsInWriteMode() {

        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_READ_ENABLE_PROPERTY, Boolean.class)).willReturn(false);
        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_WRITE_ENABLE_PROPERTY, Boolean.class)).willReturn(true);
        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_BATCH_ENABLE_PROPERTY, Boolean.class)).willReturn(false);
        given(context.getEnvironment()).willReturn(environment);

        assertTrue(underTest.matches(context, metadata));
    }

    @Test
    void testOnMessage_ShouldBlockOnMessage_WhenCorevanceIsInReadMode() {

        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_READ_ENABLE_PROPERTY, Boolean.class)).willReturn(true);
        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_WRITE_ENABLE_PROPERTY, Boolean.class)).willReturn(false);
        given(environment.getProperty(CorevanceInstanceModeConstants.COREVANCE_MODE_BATCH_ENABLE_PROPERTY, Boolean.class)).willReturn(false);
        given(context.getEnvironment()).willReturn(environment);

        assertFalse(underTest.matches(context, metadata));
    }

}
