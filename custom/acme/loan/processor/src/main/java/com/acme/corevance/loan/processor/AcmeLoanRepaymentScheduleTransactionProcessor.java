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
package com.acme.corevance.loan.processor;

import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.impl.CorevanceStyleLoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.serialization.LoanChargeValidator;
import com.corevance.portfolio.loanaccount.service.LoanBalanceService;
import org.springframework.stereotype.Component;

@Component
public class AcmeLoanRepaymentScheduleTransactionProcessor extends CorevanceStyleLoanRepaymentScheduleTransactionProcessor {

    public static final String STRATEGY_CODE = "acme-standard-strategy";

    public static final String STRATEGY_NAME = "ACME Corp.: standard loan transaction processing strategy";

    public AcmeLoanRepaymentScheduleTransactionProcessor(final ExternalIdFactory externalIdFactory,
            final LoanChargeValidator loanChargeValidator, final LoanBalanceService loanBalanceService) {
        super(externalIdFactory, loanChargeValidator, loanBalanceService);
    }

    @Override
    public String getCode() {
        return STRATEGY_CODE;
    }

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

}
