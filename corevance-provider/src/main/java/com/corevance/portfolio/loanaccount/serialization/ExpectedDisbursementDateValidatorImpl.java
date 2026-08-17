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
package com.corevance.portfolio.loanaccount.serialization;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.organisation.holiday.domain.Holiday;
import com.corevance.organisation.holiday.domain.HolidayRepositoryWrapper;
import com.corevance.organisation.holiday.service.HolidayUtil;
import com.corevance.organisation.workingdays.domain.WorkingDays;
import com.corevance.organisation.workingdays.domain.WorkingDaysRepositoryWrapper;
import com.corevance.organisation.workingdays.service.WorkingDaysUtil;
import com.corevance.portfolio.loanaccount.domain.ExpectedDisbursementDateValidator;
import com.corevance.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.springframework.stereotype.Component;

/**
 * Validates expected disbursement date: not on a non-working day or holiday when configuration disallows it.
 */
@Component
@RequiredArgsConstructor
public class ExpectedDisbursementDateValidatorImpl implements ExpectedDisbursementDateValidator {

    private final WorkingDaysRepositoryWrapper workingDaysRepository;
    private final HolidayRepositoryWrapper holidayRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Override
    public void validate(final LocalDate expectedDisbursementDate, final Long officeId) {
        if (expectedDisbursementDate == null) {
            return;
        }
        validateDisbursementDateIsOnNonWorkingDay(expectedDisbursementDate);
        if (officeId != null) {
            validateDisbursementDateIsOnHoliday(expectedDisbursementDate, officeId);
        }
    }

    private void validateDisbursementDateIsOnNonWorkingDay(final LocalDate expectedDisbursementDate) {
        final WorkingDays workingDays = workingDaysRepository.findOne();
        final boolean allowTransactionsOnNonWorkingDay = configurationDomainService.allowTransactionsOnNonWorkingDayEnabled();
        if (!allowTransactionsOnNonWorkingDay && !WorkingDaysUtil.isWorkingDay(workingDays, expectedDisbursementDate)) {
            final String errorMessage = "Expected disbursement date cannot be on a non working day";
            throw new LoanApplicationDateException("disbursement.date.on.non.working.day", errorMessage, expectedDisbursementDate);
        }
    }

    private void validateDisbursementDateIsOnHoliday(final LocalDate expectedDisbursementDate, final Long officeId) {
        final List<Holiday> holidays = holidayRepository.findByOfficeIdAndGreaterThanDate(officeId, expectedDisbursementDate);
        final boolean allowTransactionsOnHoliday = configurationDomainService.allowTransactionsOnHolidayEnabled();
        if (!allowTransactionsOnHoliday && HolidayUtil.isHoliday(expectedDisbursementDate, holidays)) {
            final String errorMessage = "Expected disbursement date cannot be on a holiday";
            throw new LoanApplicationDateException("disbursement.date.on.holiday", errorMessage, expectedDisbursementDate);
        }
    }
}
