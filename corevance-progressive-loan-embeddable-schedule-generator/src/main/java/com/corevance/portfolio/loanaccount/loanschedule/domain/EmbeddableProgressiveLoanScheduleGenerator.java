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
package com.corevance.portfolio.loanaccount.loanschedule.domain;

import java.math.MathContext;
import java.time.LocalDate;
import java.util.Optional;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.ProgressiveLoanModel;
import com.corevance.portfolio.loanaccount.loanschedule.data.LoanSchedulePlan;
import com.corevance.portfolio.loanaccount.service.InterestScheduleModelRepositoryWrapper;
import com.corevance.portfolio.loanproduct.calc.InstalmentCalculator;
import com.corevance.portfolio.loanproduct.calc.TieredInstalmentCalculator;
import com.corevance.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import com.corevance.portfolio.loanproduct.domain.ILoanConfigurationDetails;

@SuppressWarnings("unused")
public class EmbeddableProgressiveLoanScheduleGenerator {

    private final ProgressiveLoanScheduleGenerator scheduleGenerator;

    public EmbeddableProgressiveLoanScheduleGenerator() {
        final ScheduledDateGenerator scheduledDateGenerator = new DefaultScheduledDateGenerator();
        final InstalmentCalculator emiCalculator = new TieredInstalmentCalculator(scheduledDateGenerator);
        this.scheduleGenerator = new ProgressiveLoanScheduleGenerator(scheduledDateGenerator, emiCalculator,
                new NoopInterestScheduleModelRepositoryWrapper());
    }

    public LoanSchedulePlan generate(final MathContext mc, final LoanRepaymentScheduleModelData modelData) {
        return scheduleGenerator.generate(mc, modelData);
    }

    private static final class NoopInterestScheduleModelRepositoryWrapper implements InterestScheduleModelRepositoryWrapper {

        @Override
        public Optional<ProgressiveLoanModel> findOneByLoanId(Long loanId) {
            return Optional.empty();
        }

        @Override
        public Optional<ProgressiveLoanModel> findOneByLoan(Loan loan) {
            return Optional.empty();
        }

        @Override
        public Optional<ProgressiveLoanInterestScheduleModel> extractModel(Optional<ProgressiveLoanModel> progressiveLoanModel) {
            return Optional.empty();
        }

        @Override
        public ProgressiveLoanInterestScheduleModel writeInterestScheduleModel(Loan loan, ProgressiveLoanInterestScheduleModel model) {
            return null;
        }

        @Override
        public Optional<ProgressiveLoanInterestScheduleModel> readProgressiveLoanInterestScheduleModel(Long loanId,
                ILoanConfigurationDetails detail, Integer installmentAmountInMultipliesOf) {
            return Optional.empty();
        }

        @Override
        public boolean hasValidModelForDate(Long loanId, LocalDate targetDate) {
            return false;
        }

        @Override
        public Optional<ProgressiveLoanInterestScheduleModel> getSavedModel(Loan loan, LocalDate businessDate) {
            return Optional.empty();
        }

        @Override
        public Long removeByLoanId(Long loanId) {
            return 0L;
        }
    }
}
