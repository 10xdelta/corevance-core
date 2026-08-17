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
package com.corevance.portfolio.delinquency.service;

import java.util.List;
import com.corevance.portfolio.delinquency.validator.LoanDelinquencyActionData;
import com.corevance.portfolio.loanaccount.data.CollectionData;
import com.corevance.portfolio.loanaccount.data.LoanDelinquencyData;
import com.corevance.portfolio.loanaccount.domain.Loan;

public interface LoanDelinquencyDomainService {

    /**
     * This method is to calculate the Overdue date and other properties, If the loan is overdue or If there is some
     * Charge back transaction
     *
     * @param loan
     * @param effectiveDelinquencyList
     */
    CollectionData getOverdueCollectionData(Loan loan, List<LoanDelinquencyActionData> effectiveDelinquencyList);

    LoanDelinquencyData getLoanDelinquencyData(Loan loan, List<LoanDelinquencyActionData> effectiveDelinquencyList);

}
