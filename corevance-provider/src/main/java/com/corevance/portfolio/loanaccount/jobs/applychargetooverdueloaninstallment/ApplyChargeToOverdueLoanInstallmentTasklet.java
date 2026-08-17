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
package com.corevance.portfolio.loanaccount.jobs.applychargetooverdueloaninstallment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.data.ApiParameterError;
import com.corevance.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import com.corevance.infrastructure.core.exception.PlatformApiDataValidationException;
import com.corevance.infrastructure.jobs.exception.JobExecutionException;
import com.corevance.portfolio.loanaccount.loanschedule.data.OverdueLoanScheduleData;
import com.corevance.portfolio.loanaccount.service.LoanChargeWritePlatformService;
import com.corevance.portfolio.loanaccount.service.LoanReadPlatformService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class ApplyChargeToOverdueLoanInstallmentTasklet implements Tasklet {

    private final ConfigurationDomainService configurationDomainService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanChargeWritePlatformService loanChargeWritePlatformService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final Long penaltyWaitPeriodValue = configurationDomainService.retrievePenaltyWaitPeriod();
        final Boolean backdatePenalties = configurationDomainService.isBackdatePenaltiesEnabled();
        final Collection<OverdueLoanScheduleData> overdueLoanScheduledInstallments = loanReadPlatformService
                .retrieveAllLoansWithOverdueInstallments(penaltyWaitPeriodValue, backdatePenalties);

        if (!overdueLoanScheduledInstallments.isEmpty()) {
            final Map<Long, Collection<OverdueLoanScheduleData>> overdueScheduleData = new HashMap<>();
            for (final OverdueLoanScheduleData overdueInstallment : overdueLoanScheduledInstallments) {
                if (overdueScheduleData.containsKey(overdueInstallment.getLoanId())) {
                    overdueScheduleData.get(overdueInstallment.getLoanId()).add(overdueInstallment);
                } else {
                    Collection<OverdueLoanScheduleData> loanData = new ArrayList<>();
                    loanData.add(overdueInstallment);
                    overdueScheduleData.put(overdueInstallment.getLoanId(), loanData);
                }
            }

            List<Throwable> exceptions = new ArrayList<>();
            for (Map.Entry<Long, Collection<OverdueLoanScheduleData>> entry : overdueScheduleData.entrySet()) {
                try {
                    if (!entry.getValue().isEmpty()) {
                        loanChargeWritePlatformService.applyOverdueChargesForLoan(entry.getKey(), entry.getValue());
                    }

                } catch (final PlatformApiDataValidationException e) {
                    final List<ApiParameterError> errors = e.getErrors();
                    for (final ApiParameterError error : errors) {
                        log.error("Apply Charges due for overdue loans failed for account {} with message: {}", entry.getKey(),
                                error.getDeveloperMessage(), e);
                    }
                    exceptions.add(e);
                } catch (final AbstractPlatformDomainRuleException e) {
                    log.error("Apply Charges due for overdue loans failed for account {} with message: {}", entry.getKey(),
                            e.getDefaultUserMessage(), e);
                    exceptions.add(e);
                } catch (Exception e) {
                    log.error("Apply Charges due for overdue loans failed for account {}", entry.getKey(), e);
                    exceptions.add(e);
                }
            }
            if (!exceptions.isEmpty()) {
                throw new JobExecutionException(exceptions);
            }
        }
        return RepeatStatus.FINISHED;
    }
}
