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

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.data.CommandProcessingResultBuilder;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachAction;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBreachActionType;
import com.corevance.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import com.corevance.portfolio.workingcapitalloan.repository.WorkingCapitalLoanBreachActionRepository;
import com.corevance.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import com.corevance.portfolio.workingcapitalloan.validator.WorkingCapitalLoanBreachActionParseAndValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanBreachActionWriteServiceImpl implements WorkingCapitalLoanBreachActionWriteService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanBreachActionRepository actionRepository;
    private final WorkingCapitalLoanBreachActionParseAndValidator validator;
    private final WorkingCapitalLoanBreachScheduleService breachScheduleService;
    private final WorkingCapitalLoanBreachResetService breachResetService;

    @Transactional
    @Override
    public CommandProcessingResult createBreachAction(final Long workingCapitalLoanId, final JsonCommand command) {
        final WorkingCapitalLoan workingCapitalLoan = loanRepository.findById(workingCapitalLoanId)
                .orElseThrow(() -> new WorkingCapitalLoanNotFoundException(workingCapitalLoanId));

        final List<WorkingCapitalLoanBreachAction> existing = actionRepository.findByWorkingCapitalLoanIdOrderById(workingCapitalLoanId);

        final WorkingCapitalLoanBreachAction breachAction = validator.validateAndParse(command, workingCapitalLoan, existing);

        final WorkingCapitalLoanBreachAction saved = actionRepository.saveAndFlush(breachAction);
        log.debug("Created WC loan breach action {} for loan {}", breachAction.getAction(), workingCapitalLoanId);

        if (WorkingCapitalLoanBreachActionType.PAUSE.equals(breachAction.getAction())
                || WorkingCapitalLoanBreachActionType.RESUME.equals(breachAction.getAction())) {
            breachScheduleService.recalculatePeriodsForPauses(workingCapitalLoan);
        } else if (WorkingCapitalLoanBreachActionType.RESCHEDULE.equals(breachAction.getAction())) {
            breachScheduleService.rescheduleMinimumPayment(workingCapitalLoan);
        } else if (WorkingCapitalLoanBreachActionType.RESET.equals(breachAction.getAction())) {
            breachResetService.resetBreach(workingCapitalLoan, saved);
        } else if (WorkingCapitalLoanBreachActionType.UNDO_RESET.equals(breachAction.getAction())) {
            breachResetService.undoResetBreach(workingCapitalLoan, saved);
        } else if (WorkingCapitalLoanBreachActionType.ENABLE.equals(breachAction.getAction())) {
            existing.stream().filter(action -> WorkingCapitalLoanBreachActionType.DISABLE == action.getAction())
                    .reduce((first, second) -> second).ifPresent(action -> {
                        action.setEndDate(breachAction.getStartDate().minusDays(1));
                    });
            breachScheduleService.reprocessBreachSchedule(workingCapitalLoan);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(saved.getId()) //
                .withLoanId(workingCapitalLoanId) //
                .withOfficeId(workingCapitalLoan.getOfficeId()) //
                .withClientId(workingCapitalLoan.getClientId()) //
                .build();
    }

}
