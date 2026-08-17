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
package com.corevance.cob.loan;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.cob.data.COBIdAndExternalIdAndAccountNo;
import com.corevance.cob.data.LoanAccountStayedLockedData;
import com.corevance.cob.data.LoanAccountsStayedLockedData;
import com.corevance.cob.service.RetrieveIdService;
import com.corevance.infrastructure.businessdate.domain.BusinessDateType;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class StayedLockedLoansTasklet implements Tasklet {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final RetrieveIdService retrieveIdService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LoanAccountsStayedLockedData lockedLoanAccounts = buildLoanAccountData();
        if (!lockedLoanAccounts.getLoanAccounts().isEmpty()) {
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAccountsStayedLockedBusinessEvent(lockedLoanAccounts));
        }
        return RepeatStatus.FINISHED;
    }

    private LoanAccountsStayedLockedData buildLoanAccountData() {
        LocalDate cobBusinessDate = ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE);
        List<COBIdAndExternalIdAndAccountNo> stayedLockedLoanAccounts = retrieveIdService
                .findAllStayedLockedByCobBusinessDate(cobBusinessDate);
        List<LoanAccountStayedLockedData> loanAccounts = new ArrayList<>();
        stayedLockedLoanAccounts.forEach(loanAccount -> {
            loanAccounts.add(new LoanAccountStayedLockedData(loanAccount.getId(), loanAccount.getExternalId(), loanAccount.getAccountNo()));
        });
        return new LoanAccountsStayedLockedData(loanAccounts);
    }
}
