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
package com.corevance.portfolio.loanaccount.service;

import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.portfolio.loanaccount.data.TransactionChangeData;
import com.corevance.portfolio.loanaccount.domain.ChangedTransactionDetail;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;

@RequiredArgsConstructor
public class ReplayedTransactionBusinessEventServiceImpl implements ReplayedTransactionBusinessEventService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public void raiseTransactionReplayedEvents(final ChangedTransactionDetail changedTransactionDetail) {
        if (changedTransactionDetail == null || changedTransactionDetail.getTransactionChanges().isEmpty()) {
            return;
        }
        // Extra safety net to avoid event leaking
        try {
            businessEventNotifierService.startExternalEventRecording();

            for (TransactionChangeData change : changedTransactionDetail.getTransactionChanges()) {
                final LoanTransaction newTransaction = change.getNewTransaction();
                final LoanTransaction oldTransaction = change.getOldTransaction();

                if (oldTransaction != null) {
                    final LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(oldTransaction);
                    if (newTransaction.isNotReversed()) {
                        data.setNewTransactionDetail(newTransaction);
                    }
                    businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
                }
            }
            businessEventNotifierService.stopExternalEventRecording();
        } catch (Exception e) {
            businessEventNotifierService.resetEventRecording();
            throw e;
        }
    }
}
