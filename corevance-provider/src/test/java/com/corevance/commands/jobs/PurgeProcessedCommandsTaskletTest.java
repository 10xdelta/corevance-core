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
package com.corevance.commands.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import com.corevance.commands.domain.CommandSourceRepository;
import com.corevance.infrastructure.businessdate.domain.BusinessDateType;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.domain.ActionContext;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenant;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
public class PurgeProcessedCommandsTaskletTest {

    @Mock
    private CommandSourceRepository repository;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private StepContribution stepContribution;
    @Mock
    private ChunkContext chunkContext;
    private RepeatStatus resultStatus;
    private PurgeProcessedCommandsTasklet underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setTenant(new CorevancePlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        underTest = new PurgeProcessedCommandsTasklet(repository, configurationDomainService);
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void givenEventsForPurgeWhenTaskExecutionThenEventsPurgeForDaysCriteria() {
        // given
        ArgumentCaptor<OffsetDateTime> dateCriteriaCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        when(configurationDomainService.retrieveProcessedCommandsPurgeDaysCriteria()).thenReturn(2L);
        // when
        resultStatus = underTest.execute(stepContribution, chunkContext);
        // then
        verify(repository, times(1)).deleteOlderEventsWithStatus(Mockito.any(), Mockito.any());
        verify(repository).deleteOlderEventsWithStatus(Mockito.any(), dateCriteriaCaptor.capture());
        OffsetDateTime expectedDateForPurgeCriteriaTest = DateUtils.getAuditOffsetDateTime().minusDays(2);
        OffsetDateTime actualDateForPurgeCriteria = dateCriteriaCaptor.getValue();
        assertTrue(expectedDateForPurgeCriteriaTest.toEpochSecond() - actualDateForPurgeCriteria.toEpochSecond() <= 1);
        assertEquals(RepeatStatus.FINISHED, resultStatus);
    }

    @Test
    public void givenEventsForPurgeWhenExceptionOccursThenJobExecutionFinishesSuccessfully() {
        // given
        when(configurationDomainService.retrieveProcessedCommandsPurgeDaysCriteria()).thenReturn(2L);
        doThrow(new RuntimeException("Test Exception")).when(repository).deleteOlderEventsWithStatus(Mockito.any(), Mockito.any());
        // when
        resultStatus = underTest.execute(stepContribution, chunkContext);
        // then
        assertEquals(RepeatStatus.FINISHED, resultStatus);
    }
}
