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
package com.corevance.portfolio.savings.service;

import java.util.Collection;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.core.domain.CorevanceContext;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import com.corevance.infrastructure.jobs.exception.JobExecutionException;
import com.corevance.portfolio.savings.data.SavingsAccountData;

/**
 * @author manoj
 */

@Slf4j
@RequiredArgsConstructor
public class SavingsSchedularInterestPosterTask implements Callable<Void> {

    private final SavingsSchedularInterestPoster interestPoster;
    @Setter
    private CorevanceContext context;

    @Override
    public Void call() throws JobExecutionException {
        try {
            ThreadLocalContextUtil.init(context);
            interestPoster.postInterest();
            return null;
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }

    public void setSavingAccounts(Collection<SavingsAccountData> savingAccounts) {
        this.interestPoster.setSavingAccounts(savingAccounts);
    }

    public void setBackdatedTxnsAllowedTill(boolean backdatedTxnsAllowedTill) {
        this.interestPoster.setBackdatedTxnsAllowedTill(backdatedTxnsAllowedTill);
    }
}
