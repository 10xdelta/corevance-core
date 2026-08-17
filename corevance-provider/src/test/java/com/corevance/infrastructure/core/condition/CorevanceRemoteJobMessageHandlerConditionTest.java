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
package com.corevance.infrastructure.core.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

@ExtendWith(MockitoExtension.class)
class CorevanceRemoteJobMessageHandlerConditionTest {

    @Mock
    private ConditionContext conditionContext;

    @Mock
    private AnnotatedTypeMetadata metadata;

    private MockEnvironment environment;

    @InjectMocks
    private CorevanceRemoteJobMessageHandlerCondition underTest = new CorevanceRemoteJobMessageHandlerCondition();

    @BeforeEach
    public void setUp() {
        environment = new MockEnvironment();
        given(conditionContext.getEnvironment()).willReturn(environment);
    }

    @Test
    public void testMatchesShouldReturnFalseWhenSpringEventsIsEnabledAndManagerAndWorker() {
        // given
        environment.withProperty("corevance.remote-job-message-handler.spring-events.enabled", "true");
        environment.withProperty("corevance.remote-job-message-handler.jms.enabled", "false");
        environment.withProperty("corevance.mode.batch-manager-enabled", "true");
        environment.withProperty("corevance.mode.batch-worker-enabled", "true");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testMatchesShouldReturnTrueWhenSpringEventsIsEnabledAndManagerOnly() {
        // given
        environment.withProperty("corevance.remote-job-message-handler.spring-events.enabled", "true");
        environment.withProperty("corevance.remote-job-message-handler.jms.enabled", "false");
        environment.withProperty("corevance.mode.batch-manager-enabled", "true");
        environment.withProperty("corevance.mode.batch-worker-enabled", "false");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testMatchesShouldReturnTrueWhenSpringEventsIsEnabledAndWorkerOnly() {
        // given
        environment.withProperty("corevance.remote-job-message-handler.spring-events.enabled", "true");
        environment.withProperty("corevance.remote-job-message-handler.jms.enabled", "false");
        environment.withProperty("corevance.mode.batch-manager-enabled", "false");
        environment.withProperty("corevance.mode.batch-worker-enabled", "true");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testMatchesShouldReturnFalseWhenJmsIsEnabledAndManagerAndWorker() {
        // given
        environment.withProperty("corevance.remote-job-message-handler.spring-events.enabled", "false");
        environment.withProperty("corevance.remote-job-message-handler.jms.enabled", "true");
        environment.withProperty("corevance.mode.batch-manager-enabled", "true");
        environment.withProperty("corevance.mode.batch-worker-enabled", "true");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testMatchesShouldReturnFalseWhenJmsIsEnabledAndManagerOnly() {
        // given
        environment.withProperty("corevance.remote-job-message-handler.spring-events.enabled", "false");
        environment.withProperty("corevance.remote-job-message-handler.jms.enabled", "true");
        environment.withProperty("corevance.mode.batch-manager-enabled", "true");
        environment.withProperty("corevance.mode.batch-worker-enabled", "false");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testMatchesShouldReturnFalseWhenJmsIsEnabledAndWorkerOnly() {
        // given
        environment.withProperty("corevance.remote-job-message-handler.spring-events.enabled", "false");
        environment.withProperty("corevance.remote-job-message-handler.jms.enabled", "true");
        environment.withProperty("corevance.mode.batch-manager-enabled", "false");
        environment.withProperty("corevance.mode.batch-worker-enabled", "true");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isFalse();
    }

    @Test
    public void testMatchesShouldReturnTrueWhenSpringEventsIsEnabledAndJmsIsEnabled() {
        // given
        environment.withProperty("corevance.remote-job-message-handler.spring-events.enabled", "true");
        environment.withProperty("corevance.remote-job-message-handler.jms.enabled", "true");
        environment.withProperty("corevance.mode.batch-manager-enabled", "true");
        environment.withProperty("corevance.mode.batch-worker-enabled", "true");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isTrue();
    }

    @Test
    public void testMatchesShouldReturnFalseWhenNoMessagingIsConfiguredAndNotManagerOrWorker() {
        // given
        environment.withProperty("corevance.remote-job-message-handler.spring-events.enabled", "false");
        environment.withProperty("corevance.remote-job-message-handler.jms.enabled", "false");
        environment.withProperty("corevance.mode.batch-manager-enabled", "false");
        environment.withProperty("corevance.mode.batch-worker-enabled", "false");
        // when
        boolean result = underTest.matches(conditionContext, metadata);
        // then
        assertThat(result).isFalse();
    }
}
