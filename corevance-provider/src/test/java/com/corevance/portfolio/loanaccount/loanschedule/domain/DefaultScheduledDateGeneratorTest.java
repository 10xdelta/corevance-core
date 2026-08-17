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

import static java.math.BigDecimal.ZERO;
import static java.util.Collections.EMPTY_LIST;
import static com.corevance.organisation.monetary.domain.MonetaryCurrency.fromApplicationCurrency;
import static com.corevance.organisation.workingdays.domain.RepaymentRescheduleType.MOVE_TO_NEXT_WORKING_DAY;
import static com.corevance.portfolio.calendar.service.CalendarUtils.FLOATING_TIMEZONE_PROPERTY_KEY;
import static com.corevance.portfolio.common.domain.DayOfWeekType.INVALID;
import static com.corevance.portfolio.common.domain.PeriodFrequencyType.MONTHS;
import static com.corevance.portfolio.loanaccount.loanschedule.domain.LoanScheduleType.CUMULATIVE;
import static com.corevance.portfolio.loanproduct.domain.AmortizationMethod.EQUAL_PRINCIPAL;
import static com.corevance.portfolio.loanproduct.domain.InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD;
import static com.corevance.portfolio.loanproduct.domain.InterestMethod.FLAT;
import static com.corevance.portfolio.loanproduct.domain.LoanPreCloseInterestCalculationStrategy.NONE;
import static com.corevance.portfolio.loanproduct.domain.RepaymentStartDateType.DISBURSEMENT_DATE;
import static com.corevance.util.TimeZoneConstants.ASIA_MANILA_ID;
import static com.corevance.util.TimeZoneConstants.EUROPE_BERLIN_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import com.corevance.junit.context.WithTenantContext;
import com.corevance.junit.context.WithTenantContextExtension;
import com.corevance.junit.system.WithSystemProperty;
import com.corevance.junit.system.WithSystemPropertyExtension;
import com.corevance.junit.timezone.WithSystemTimeZone;
import com.corevance.junit.timezone.WithSystemTimeZoneExtension;
import com.corevance.organisation.monetary.domain.ApplicationCurrency;
import com.corevance.organisation.monetary.domain.Money;
import com.corevance.organisation.monetary.domain.MoneyHelper;
import com.corevance.organisation.workingdays.data.AdjustedDateDetailsDTO;
import com.corevance.organisation.workingdays.domain.WorkingDays;
import com.corevance.portfolio.common.domain.DaysInMonthType;
import com.corevance.portfolio.common.domain.DaysInYearType;
import com.corevance.portfolio.loanaccount.data.HolidayDetailDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({ WithSystemTimeZoneExtension.class, WithTenantContextExtension.class, WithSystemPropertyExtension.class })
public class DefaultScheduledDateGeneratorTest {

    private DefaultScheduledDateGenerator underTest = new DefaultScheduledDateGenerator();

    @BeforeEach
    public void setUp() {
        // Initialize MoneyHelper with default rounding mode (HALF_EVEN = 6)
        MoneyHelper.initializeTenantRoundingMode("default", 6);
    }

    @Test
    @WithSystemTimeZone(EUROPE_BERLIN_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_generateRepaymentPeriods() {
        // given
        HolidayDetailDTO holidayDetailDTO = createHolidayDTO();
        MathContext mathContext = new MathContext(12, RoundingMode.HALF_EVEN);

        ApplicationCurrency dollarCurrency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
        Money principalAmount = Money.of(fromApplicationCurrency(dollarCurrency), BigDecimal.valueOf(100));
        LocalDate expectedDisbursementDate = LocalDate.of(2024, 1, 1);
        LocalDate dueRepaymentPeriodDate = LocalDate.of(2024, 2, 1);

        LocalDate submittedOnDate = LocalDate.of(2024, 1, 1);
        LoanApplicationTerms loanApplicationTerms = LoanApplicationTerms.assembleFrom(dollarCurrency.toData(), 1, MONTHS, 4, 1, MONTHS,
                null, INVALID, EQUAL_PRINCIPAL, FLAT, ZERO, MONTHS, ZERO, SAME_AS_REPAYMENT_PERIOD, false, principalAmount,
                expectedDisbursementDate, null, dueRepaymentPeriodDate, null, null, null, null, null,
                Money.of(fromApplicationCurrency(dollarCurrency), ZERO), false, null, EMPTY_LIST, BigDecimal.valueOf(36_000L), null,
                DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, false, null, null, null, null, null, ZERO, null, NONE, null, ZERO,
                EMPTY_LIST, true, 0, false, holidayDetailDTO, false, false, false, null, false, false, null, false, DISBURSEMENT_DATE,
                submittedOnDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL, null, false, null, null, false, null, false, null, null,
                null, false, null, null, null, false, false);

        // when
        List<? extends LoanScheduleModelPeriod> result = underTest.generateRepaymentPeriods(mathContext, expectedDisbursementDate,
                loanApplicationTerms, holidayDetailDTO);

        // then
        assertThat(result).hasSize(4);
        assertThat(result).satisfies(periods -> {
            LoanScheduleModelPeriod firstPeriod = periods.get(0);
            assertThat(firstPeriod.periodNumber()).isEqualTo(1);
            assertThat(firstPeriod.periodFromDate()).hasToString("2024-01-01");
            assertThat(firstPeriod.periodDueDate()).hasToString("2024-02-01");

            LoanScheduleModelPeriod secondPeriod = periods.get(1);
            assertThat(secondPeriod.periodNumber()).isEqualTo(2);
            assertThat(secondPeriod.periodFromDate()).hasToString("2024-02-01");
            assertThat(secondPeriod.periodDueDate()).hasToString("2024-03-01");

            LoanScheduleModelPeriod thirdPeriod = periods.get(2);
            assertThat(thirdPeriod.periodNumber()).isEqualTo(3);
            assertThat(thirdPeriod.periodFromDate()).hasToString("2024-03-01");
            assertThat(thirdPeriod.periodDueDate()).hasToString("2024-04-01");

            LoanScheduleModelPeriod fourthPeriod = periods.get(3);
            assertThat(fourthPeriod.periodNumber()).isEqualTo(4);
            assertThat(fourthPeriod.periodFromDate()).hasToString("2024-04-01");
            assertThat(fourthPeriod.periodDueDate()).hasToString("2024-05-01");
        });
    }

    @Test
    @WithSystemTimeZone(EUROPE_BERLIN_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_AdjustRepaymentDate_Works_WithSameTenant_And_SystemTimeZone() {
        // given
        LocalDate dueRepaymentPeriodDate = LocalDate.of(2023, 11, 26);

        LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(dueRepaymentPeriodDate, createHolidayDTO());
        // when
        AdjustedDateDetailsDTO result = underTest.adjustRepaymentDate(dueRepaymentPeriodDate, loanApplicationTerms, createHolidayDTO());
        // then
        assertThat(result).satisfies(r -> {
            assertThat(r.getChangedScheduleDate()).isEqualTo(LocalDate.of(2023, 11, 26));
            assertThat(r.getChangedActualRepaymentDate()).isEqualTo(LocalDate.of(2023, 11, 26));
            assertThat(r.getNextRepaymentPeriodDueDate()).isEqualTo(LocalDate.of(2023, 12, 26));
        });
    }

    @Test
    @WithSystemTimeZone(ASIA_MANILA_ID)
    @WithTenantContext(tenantTimeZoneId = EUROPE_BERLIN_ID)
    @WithSystemProperty(key = FLOATING_TIMEZONE_PROPERTY_KEY, value = "true")
    public void test_AdjustRepaymentDate_Works_WithDifferentTenant_And_SystemTimeZone() {
        // given
        LocalDate dueRepaymentPeriodDate = LocalDate.of(2023, 11, 26);

        LoanApplicationTerms loanApplicationTerms = createLoanApplicationTerms(dueRepaymentPeriodDate, createHolidayDTO());
        // when
        AdjustedDateDetailsDTO result = underTest.adjustRepaymentDate(dueRepaymentPeriodDate, loanApplicationTerms, createHolidayDTO());
        // then
        assertThat(result).satisfies(r -> {
            assertThat(r.getChangedScheduleDate()).isEqualTo(LocalDate.of(2023, 11, 26));
            assertThat(r.getChangedActualRepaymentDate()).isEqualTo(LocalDate.of(2023, 11, 26));
            assertThat(r.getNextRepaymentPeriodDueDate()).isEqualTo(LocalDate.of(2023, 12, 26));
        });
    }

    private LoanApplicationTerms createLoanApplicationTerms(LocalDate dueRepaymentPeriodDate, HolidayDetailDTO holidayDetailDTO) {
        ApplicationCurrency dollarCurrency = new ApplicationCurrency("USD", "US Dollar", 2, 0, "currency.USD", "$");
        Money principalAmount = Money.of(fromApplicationCurrency(dollarCurrency), BigDecimal.valueOf(1000L));
        LocalDate expectedDisbursementDate = LocalDate.of(2023, 10, 26);

        LocalDate submittedOnDate = LocalDate.of(2023, 10, 24);
        return LoanApplicationTerms.assembleFrom(dollarCurrency.toData(), 1, MONTHS, 1, 1, MONTHS, null, INVALID, EQUAL_PRINCIPAL, FLAT,
                ZERO, MONTHS, ZERO, SAME_AS_REPAYMENT_PERIOD, false, principalAmount, expectedDisbursementDate, null,
                dueRepaymentPeriodDate, null, null, null, null, null, Money.of(fromApplicationCurrency(dollarCurrency), ZERO), false, null,
                EMPTY_LIST, BigDecimal.valueOf(36_000L), null, DaysInMonthType.ACTUAL, DaysInYearType.ACTUAL, false, null, null, null, null,
                null, ZERO, null, NONE, null, ZERO, EMPTY_LIST, true, 0, false, holidayDetailDTO, false, false, false, null, false, false,
                null, false, DISBURSEMENT_DATE, submittedOnDate, CUMULATIVE, LoanScheduleProcessingType.HORIZONTAL, null, false, null, null,
                false, null, false, null, null, null, false, null, null, null, false, false);
    }

    private HolidayDetailDTO createHolidayDTO() {
        return new HolidayDetailDTO(false, EMPTY_LIST,
                new WorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU", MOVE_TO_NEXT_WORKING_DAY.getValue(), false, false),
                false, false);
    }
}
