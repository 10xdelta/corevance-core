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
package com.corevance.portfolio.workingcapitalloan.service;

import java.math.MathContext;
import lombok.RequiredArgsConstructor;
import com.corevance.organisation.monetary.data.CurrencyData;
import com.corevance.organisation.monetary.domain.MoneyHelper;
import com.corevance.portfolio.workingcapitalloan.calc.ProjectedAmortizationScheduleModel;
import com.corevance.portfolio.workingcapitalloan.data.ProjectedAmortizationScheduleData;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import com.corevance.portfolio.workingcapitalloan.exception.ProjectedAmortizationScheduleNotFoundException;
import com.corevance.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import com.corevance.portfolio.workingcapitalloan.mapper.ProjectedAmortizationScheduleMapper;
import com.corevance.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanAmortizationScheduleReadServiceImpl implements WorkingCapitalLoanAmortizationScheduleReadService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final ProjectedAmortizationScheduleRepositoryWrapper scheduleRepositoryWrapper;
    private final ProjectedAmortizationScheduleMapper mapper;

    @Override
    public ProjectedAmortizationScheduleData retrieveAmortizationSchedule(final Long loanId) {
        final WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        final MathContext mc = MoneyHelper.getMathContext();
        final CurrencyData currency = WorkingCapitalLoanCurrencyResolver.resolveCurrency(loan);
        final ProjectedAmortizationScheduleModel model = scheduleRepositoryWrapper.readModel(loanId, mc, currency)
                .orElseThrow(() -> new ProjectedAmortizationScheduleNotFoundException(loanId));

        return mapper.toData(model);
    }
}
