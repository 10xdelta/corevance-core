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

import java.math.BigDecimal;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.data.CommandProcessingResultBuilder;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import com.corevance.portfolio.workingcapitalloan.domain.NearBreachActionType;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanNearBreachAction;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import com.corevance.portfolio.workingcapitalloan.exception.WorkingCapitalLoanNotFoundException;
import com.corevance.portfolio.workingcapitalloan.repository.WorkingCapitalLoanNearBreachActionRepository;
import com.corevance.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import com.corevance.portfolio.workingcapitalloan.serialization.WorkingCapitalLoanDataValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingCapitalLoanNearBreachActionWriteServiceImpl implements WorkingCapitalLoanNearBreachActionWriteService {

    private final WorkingCapitalLoanRepository loanRepository;
    private final WorkingCapitalLoanNearBreachActionRepository actionRepository;
    private final WorkingCapitalLoanDataValidator validator;
    private final FromJsonHelper fromApiJsonHelper;

    @Transactional
    @Override
    public CommandProcessingResult createNearBreachAction(final Long loanId, final JsonCommand command) {
        final WorkingCapitalLoan loan = loanRepository.findById(loanId).orElseThrow(() -> new WorkingCapitalLoanNotFoundException(loanId));

        validator.validateNearBreachAction(command.json(), loan);

        final String actionStr = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.nearBreachActionParamName,
                command.parsedJson());
        final NearBreachActionType actionType = NearBreachActionType.valueOf(actionStr);

        final BigDecimal threshold = fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanConstants.nearBreachThresholdParamName,
                command.parsedJson(), new HashSet<>());
        final Integer frequency = fromApiJsonHelper.extractIntegerNamed(WorkingCapitalLoanConstants.nearBreachFrequencyParamName,
                command.parsedJson(), new HashSet<>());
        final String frequencyTypeStr = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.nearBreachFrequencyTypeParamName,
                command.parsedJson());
        final WorkingCapitalLoanPeriodFrequencyType frequencyType = frequencyTypeStr != null
                ? WorkingCapitalLoanPeriodFrequencyType.fromString(frequencyTypeStr)
                : null;

        final WorkingCapitalLoanNearBreachAction action = WorkingCapitalLoanNearBreachAction.create(loan, actionType, threshold, frequency,
                frequencyType);
        final WorkingCapitalLoanNearBreachAction saved = actionRepository.saveAndFlush(action);

        log.debug("Created near breach action {} ({}) for WC loan {}", saved.getId(), actionType, loanId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(saved.getId()) //
                .withLoanId(loanId) //
                .withOfficeId(loan.getOfficeId()) //
                .withClientId(loan.getClientId()) //
                .build();
    }
}
