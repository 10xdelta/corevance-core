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
package com.corevance.cob.service;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import com.corevance.cob.conditions.LoanCOBEnabledCondition;
import com.corevance.cob.domain.LoanAccountLock;
import com.corevance.cob.domain.LoanAccountLockRepository;
import com.corevance.cob.domain.LockOwner;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.jobs.domain.CustomJobParameterRepository;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@Conditional(LoanCOBEnabledCondition.class)
public class InlineLoanCOBExecutorServiceImpl extends InlineCommonLockableCOBExecutorService<LoanAccountLock> {

    public InlineLoanCOBExecutorServiceImpl(LoanAccountLockRepository loanAccountLockRepository,
            InlineLoanCOBExecutionDataParser dataParser, JobLauncher jobLauncher, JobLocator jobLocator, JobExplorer jobExplorer,
            @Qualifier("requiresNewTransactionTemplate") TransactionTemplate requiresNewTransactionTemplate,
            CustomJobParameterRepository customJobParameterRepository, PlatformSecurityContext context,
            RetrieveLoanIdService retrieveIdService, CorevanceProperties corevanceProperties) {
        super(loanAccountLockRepository, dataParser, jobLauncher, jobLocator, jobExplorer, requiresNewTransactionTemplate,
                customJobParameterRepository, context, retrieveIdService, corevanceProperties);
    }

    @Override
    public LoanAccountLock createAccountLock(Long loanId, LockOwner loanInlineCobProcessing, LocalDate businessDate) {
        return new LoanAccountLock(loanId, LockOwner.LOAN_INLINE_COB_PROCESSING, businessDate);
    }
}
