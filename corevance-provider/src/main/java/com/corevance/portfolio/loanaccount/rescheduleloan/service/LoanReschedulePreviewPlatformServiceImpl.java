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
package com.corevance.portfolio.loanaccount.rescheduleloan.service;

import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.organisation.monetary.domain.MoneyHelper;
import com.corevance.portfolio.loanaccount.data.LoanTermVariationsData;
import com.corevance.portfolio.loanaccount.data.ScheduleGeneratorDTO;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import com.corevance.portfolio.loanaccount.domain.LoanRescheduleRequestToTermVariationMapping;
import com.corevance.portfolio.loanaccount.domain.LoanTermVariations;
import com.corevance.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import com.corevance.portfolio.loanaccount.loanschedule.data.LoanScheduleDTO;
import com.corevance.portfolio.loanaccount.loanschedule.domain.DefaultScheduledDateGenerator;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanScheduleGenerator;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanScheduleGeneratorFactory;
import com.corevance.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import com.corevance.portfolio.loanaccount.mapper.LoanTermVariationsMapper;
import com.corevance.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequest;
import com.corevance.portfolio.loanaccount.rescheduleloan.domain.LoanRescheduleRequestRepositoryWrapper;
import com.corevance.portfolio.loanaccount.rescheduleloan.exception.LoanRescheduleRequestNotFoundException;
import com.corevance.portfolio.loanaccount.service.LoanUtilService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanReschedulePreviewPlatformServiceImpl implements LoanReschedulePreviewPlatformService {

    private static final DefaultScheduledDateGenerator DEFAULT_SCHEDULED_DATE_GENERATOR = new DefaultScheduledDateGenerator();
    private final LoanRescheduleRequestRepositoryWrapper loanRescheduleRequestRepository;
    private final LoanUtilService loanUtilService;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final LoanScheduleGeneratorFactory loanScheduleFactory;
    private final LoanTermVariationsMapper loanTermVariationsMapper;

    @Override
    public LoanScheduleModel previewLoanReschedule(Long requestId) {
        final LoanRescheduleRequest loanRescheduleRequest = this.loanRescheduleRequestRepository.findOneWithNotFoundDetection(requestId,
                true);

        if (loanRescheduleRequest == null) {
            throw new LoanRescheduleRequestNotFoundException(requestId);
        }

        Loan loan = loanRescheduleRequest.getLoan();

        ScheduleGeneratorDTO scheduleGeneratorDTO = this.loanUtilService.buildScheduleGeneratorDTO(loan,
                loanRescheduleRequest.getRescheduleFromDate());
        LocalDate rescheduleFromDate = null;
        List<LoanTermVariationsData> removeLoanTermVariationsData = new ArrayList<>();
        final LoanApplicationTerms loanApplicationTerms = loanTermVariationsMapper.constructLoanApplicationTerms(scheduleGeneratorDTO,
                loan);
        LoanTermVariations dueDateVariationInCurrentRequest = loanRescheduleRequest.getDueDateTermVariationIfExists();
        if (dueDateVariationInCurrentRequest != null) {
            for (LoanTermVariationsData loanTermVariation : loanApplicationTerms.getLoanTermVariations().getDueDateVariation()) {
                if (loanTermVariation.getDateValue().equals(dueDateVariationInCurrentRequest.fetchTermApplicaDate())) {
                    rescheduleFromDate = loanTermVariation.getTermVariationApplicableFrom();
                    removeLoanTermVariationsData.add(loanTermVariation);
                }
            }
        }
        loanApplicationTerms.getLoanTermVariations().getDueDateVariation().removeAll(removeLoanTermVariationsData);
        if (rescheduleFromDate == null) {
            rescheduleFromDate = loanRescheduleRequest.getRescheduleFromDate();
        }
        List<LoanTermVariationsData> loanTermVariationsData = new ArrayList<>();
        LocalDate adjustedApplicableDate = null;
        Set<LoanRescheduleRequestToTermVariationMapping> loanRescheduleRequestToTermVariationMappings = loanRescheduleRequest
                .getLoanRescheduleRequestToTermVariationMappings();
        if (!loanRescheduleRequestToTermVariationMappings.isEmpty()) {
            for (LoanRescheduleRequestToTermVariationMapping loanRescheduleRequestToTermVariationMapping : loanRescheduleRequestToTermVariationMappings) {
                if (loanRescheduleRequestToTermVariationMapping.getLoanTermVariations().getTermType().isDueDateVariation()
                        && rescheduleFromDate != null) {
                    adjustedApplicableDate = loanRescheduleRequestToTermVariationMapping.getLoanTermVariations().fetchDateValue();
                    loanRescheduleRequestToTermVariationMapping.getLoanTermVariations().setTermApplicableFrom(rescheduleFromDate);
                }
                loanTermVariationsData.add(loanRescheduleRequestToTermVariationMapping.getLoanTermVariations().toData());
            }
        }

        for (LoanTermVariationsData loanTermVariation : loanApplicationTerms.getLoanTermVariations().getDueDateVariation()) {
            if (DateUtils.isBefore(rescheduleFromDate, loanTermVariation.getTermVariationApplicableFrom())) {
                LocalDate applicableDate = DEFAULT_SCHEDULED_DATE_GENERATOR.generateNextRepaymentDate(rescheduleFromDate,
                        loanApplicationTerms, false);
                if (DateUtils.isEqual(loanTermVariation.getTermVariationApplicableFrom(), applicableDate)) {
                    LocalDate adjustedDate = DEFAULT_SCHEDULED_DATE_GENERATOR.generateNextRepaymentDate(adjustedApplicableDate,
                            loanApplicationTerms, false);
                    loanTermVariation.setApplicableFromDate(adjustedDate);
                    loanTermVariationsData.add(loanTermVariation);
                }
            }
        }

        loanApplicationTerms.getLoanTermVariations().updateLoanTermVariationsData(loanTermVariationsData);

        final MathContext mathContext = MoneyHelper.getMathContext();
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = this.loanRepaymentScheduleTransactionProcessorFactory
                .determineProcessor(loan.transactionProcessingStrategy());
        final LoanScheduleGenerator loanScheduleGenerator = this.loanScheduleFactory.create(loanApplicationTerms.getLoanScheduleType(),
                loanApplicationTerms.getInterestMethod());
        final LoanScheduleDTO loanSchedule = loanScheduleGenerator.rescheduleNextInstallments(mathContext, loanApplicationTerms, loan,
                loanApplicationTerms.getHolidayDetailDTO(), loanRepaymentScheduleTransactionProcessor, rescheduleFromDate);
        final LoanScheduleModel loanScheduleModel = loanSchedule.getLoanScheduleModel();

        return LoanScheduleModel.withLoanScheduleModelPeriods(loanScheduleModel.getPeriods(), loanScheduleModel);
    }

}
