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
import java.util.List;
import com.corevance.organisation.workingdays.data.AdjustedDateDetailsDTO;
import com.corevance.portfolio.calendar.domain.Calendar;
import com.corevance.portfolio.common.domain.PeriodFrequencyType;
import com.corevance.portfolio.loanaccount.data.HolidayDetailDTO;

public interface ScheduledDateGenerator {

    List<LoanScheduleModelRepaymentPeriod> generateRepaymentPeriods(MathContext mc, LocalDate scheduledDueDate,
            LoanApplicationTerms loanApplicationTerms, HolidayDetailDTO holidayDetailDTO);

    LocalDate getLastRepaymentDate(LoanApplicationTerms loanApplicationTerms, HolidayDetailDTO holidayDetailDTO);

    LocalDate idealDisbursementDateBasedOnFirstRepaymentDate(PeriodFrequencyType repaymentPeriodFrequencyType, int repaidEvery,
            LocalDate firstRepaymentDate, Calendar loanCalendar, HolidayDetailDTO holidayDetailDTO,
            LoanApplicationTerms loanApplicationTerms);

    LocalDate generateNextRepaymentDate(LocalDate lastRepaymentDate, LoanApplicationTerms loanApplicationTerms, boolean isFirstRepayment);

    LocalDate generateNextRepaymentDate(LocalDate lastRepaymentDate, LoanApplicationTerms loanApplicationTerms, boolean isFirstRepayment,
            Integer periodNumber);

    AdjustedDateDetailsDTO adjustRepaymentDate(LocalDate dueRepaymentPeriodDate, LoanApplicationTerms loanApplicationTerms,
            HolidayDetailDTO holidayDetailDTO);

    LocalDate getRepaymentPeriodDate(PeriodFrequencyType frequency, int repaidEvery, LocalDate startDate);

    Boolean isDateFallsInSchedule(PeriodFrequencyType frequency, int repaidEvery, LocalDate startDate, LocalDate date);

    LocalDate generateNextScheduleDateStartingFromDisburseDate(LocalDate lastRepaymentDate, LoanApplicationTerms loanApplicationTerms,
            HolidayDetailDTO holidayDetailDTO);

    LocalDate generateNextScheduleDateStartingFromDisburseDateOrRescheduleDate(LocalDate lastRepaymentDate,
            LoanApplicationTerms loanApplicationTerms, HolidayDetailDTO holidayDetailDTO);
}
