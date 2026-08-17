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
package com.corevance.portfolio.loanaccount.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.portfolio.calendar.data.CalendarHistoryDataWrapper;
import com.corevance.portfolio.calendar.domain.Calendar;
import com.corevance.portfolio.calendar.domain.CalendarInstance;
import com.corevance.portfolio.calendar.service.CalendarUtils;
import com.corevance.portfolio.common.domain.DayOfWeekType;
import com.corevance.portfolio.common.domain.NthDayType;
import com.corevance.portfolio.floatingrates.data.FloatingRateDTO;
import com.corevance.portfolio.floatingrates.data.FloatingRatePeriodData;
import com.corevance.portfolio.loanaccount.data.DisbursementData;
import com.corevance.portfolio.loanaccount.data.HolidayDetailDTO;
import com.corevance.portfolio.loanaccount.data.LoanTermVariationsData;
import com.corevance.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanDisbursementDetails;
import com.corevance.portfolio.loanaccount.domain.LoanTermVariationType;
import com.corevance.portfolio.loanaccount.domain.LoanTermVariations;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import com.corevance.portfolio.loanproduct.domain.InterestRecalculationCompoundingMethod;
import com.corevance.portfolio.loanproduct.domain.LoanRescheduleStrategyMethod;
import com.corevance.portfolio.loanproduct.domain.RecalculationFrequencyType;
import com.corevance.portfolio.loanproduct.domain.RepaymentStartDateType;
import com.corevance.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.stereotype.Component;

@Component
public class LoanTermVariationsMapper {

    public BigDecimal constructLoanTermVariations(final FloatingRateDTO floatingRateDTO, final BigDecimal annualNominalInterestRate,
            final List<LoanTermVariationsData> loanTermVariations, final Loan loan) {
        for (LoanTermVariations variationTerms : loan.getLoanTermVariations()) {
            if (variationTerms.isActive()) {
                loanTermVariations.add(variationTerms.toData());
            }
        }

        return constructFloatingInterestRates(annualNominalInterestRate, floatingRateDTO, loanTermVariations, loan);
    }

    public LoanApplicationTerms constructLoanApplicationTerms(final ScheduleGeneratorDTO scheduleGeneratorDTO, final Loan loan) {
        final Integer loanTermFrequency = loan.getTermFrequency();
        NthDayType nthDayType = null;
        DayOfWeekType dayOfWeekType = null;
        final List<DisbursementData> disbursementData = new ArrayList<>();
        for (LoanDisbursementDetails disbursementDetails : loan.getDisbursementDetails()) {
            disbursementData.add(disbursementDetails.toData());
        }

        Calendar calendar = scheduleGeneratorDTO.getCalendar();
        if (calendar != null) {
            nthDayType = CalendarUtils.getRepeatsOnNthDayOfMonth(calendar.getRecurrence());
            dayOfWeekType = DayOfWeekType.fromInt(CalendarUtils.getRepeatsOnDay(calendar.getRecurrence()).getValue());
        }
        HolidayDetailDTO holidayDetailDTO = scheduleGeneratorDTO.getHolidayDetailDTO();
        CalendarInstance restCalendarInstance = null;
        CalendarInstance compoundingCalendarInstance = null;
        RecalculationFrequencyType recalculationFrequencyType = null;
        InterestRecalculationCompoundingMethod compoundingMethod = null;
        RecalculationFrequencyType compoundingFrequencyType = null;
        LoanRescheduleStrategyMethod rescheduleStrategyMethod = null;
        CalendarHistoryDataWrapper calendarHistoryDataWrapper;
        RepaymentStartDateType repaymentStartDateType = loan.getRepaymentStartDateType();
        boolean allowCompoundingOnEod = false;
        if (loan.getLoanProductRelatedDetail().isInterestRecalculationEnabled()) {
            restCalendarInstance = scheduleGeneratorDTO.getCalendarInstanceForInterestRecalculation();
            compoundingCalendarInstance = scheduleGeneratorDTO.getCompoundingCalendarInstance();
            recalculationFrequencyType = loan.getLoanInterestRecalculationDetails().getRestFrequencyType();
            compoundingMethod = loan.getLoanInterestRecalculationDetails().getInterestRecalculationCompoundingMethod();
            compoundingFrequencyType = loan.getLoanInterestRecalculationDetails().getCompoundingFrequencyType();
            rescheduleStrategyMethod = loan.getLoanInterestRecalculationDetails().getRescheduleStrategyMethod();
            allowCompoundingOnEod = loan.getLoanInterestRecalculationDetails().allowCompoundingOnEod();
        }
        calendar = scheduleGeneratorDTO.getCalendar();
        calendarHistoryDataWrapper = scheduleGeneratorDTO.getCalendarHistoryDataWrapper();

        BigDecimal annualNominalInterestRate = loan.getLoanRepaymentScheduleDetail().getAnnualNominalInterestRate();
        FloatingRateDTO floatingRateDTO = scheduleGeneratorDTO.getFloatingRateDTO();
        List<LoanTermVariationsData> loanTermVariations = new ArrayList<>();
        annualNominalInterestRate = constructLoanTermVariations(floatingRateDTO, annualNominalInterestRate, loanTermVariations, loan);
        LocalDate interestChargedFromDate = loan.getInterestChargedFromDate();
        if (interestChargedFromDate == null && scheduleGeneratorDTO.isInterestChargedFromDateAsDisbursementDateEnabled()) {
            interestChargedFromDate = loan.getDisbursementDate();
        }

        return LoanApplicationTerms.assembleFrom(scheduleGeneratorDTO.getCurrency(), loanTermFrequency, loan.getTermPeriodFrequencyType(),
                nthDayType, dayOfWeekType, loan.getDisbursementDate(), loan.getExpectedFirstRepaymentOnDate(),
                scheduleGeneratorDTO.getCalculatedRepaymentsStartingFromDate(), loan.getInArrearsTolerance(),
                loan.getLoanRepaymentScheduleDetail(), loan.getLoanProduct().isMultiDisburseLoan(), loan.getFixedEmiAmount(),
                disbursementData, loan.getMaxOutstandingLoanBalance(), interestChargedFromDate,
                loan.getLoanProduct().getPrincipalThresholdForLastInstallment(),
                loan.getLoanProductRelatedDetail().getInstallmentAmountInMultiplesOf(), recalculationFrequencyType, restCalendarInstance,
                compoundingMethod, compoundingCalendarInstance, compoundingFrequencyType,
                loan.getLoanProduct().preCloseInterestCalculationStrategy(), rescheduleStrategyMethod, calendar,
                loan.getApprovedPrincipal(), annualNominalInterestRate, loanTermVariations, calendarHistoryDataWrapper,
                scheduleGeneratorDTO.getNumberOfdays(), scheduleGeneratorDTO.isSkipRepaymentOnFirstDayofMonth(), holidayDetailDTO,
                allowCompoundingOnEod, scheduleGeneratorDTO.isFirstRepaymentDateAllowedOnHoliday(),
                scheduleGeneratorDTO.isInterestToBeRecoveredFirstWhenGreaterThanEMI(), loan.getFixedPrincipalPercentagePerInstallment(),
                scheduleGeneratorDTO.isPrincipalCompoundingDisabledForOverdueLoans(), repaymentStartDateType, loan.getSubmittedOnDate(),
                loan.isAllowFullTermForTranche());
    }

    private BigDecimal constructFloatingInterestRates(final BigDecimal annualNominalInterestRate, final FloatingRateDTO floatingRateDTO,
            final List<LoanTermVariationsData> loanTermVariations, final Loan loan) {
        final LocalDate dateValue = null;
        final boolean isSpecificToInstallment = false;
        BigDecimal interestRate = annualNominalInterestRate;
        if (loan.getLoanProduct().isLinkedToFloatingInterestRate()) {
            floatingRateDTO.resetInterestRateDiff();
            Collection<FloatingRatePeriodData> applicableRates = loan.getLoanProduct().fetchInterestRates(floatingRateDTO);
            LocalDate interestRateStartDate = DateUtils.getBusinessLocalDate();
            for (FloatingRatePeriodData periodData : applicableRates) {
                LoanTermVariationsData loanTermVariation = new LoanTermVariationsData(
                        LoanEnumerations.loanVariationType(LoanTermVariationType.INTEREST_RATE), periodData.getFromDateAsLocalDate(),
                        periodData.getInterestRate(), dateValue, isSpecificToInstallment);
                if (!DateUtils.isBefore(interestRateStartDate, periodData.getFromDateAsLocalDate())) {
                    interestRateStartDate = periodData.getFromDateAsLocalDate();
                    interestRate = periodData.getInterestRate();
                }
                loanTermVariations.add(loanTermVariation);
            }
        }
        return interestRate;
    }

}
