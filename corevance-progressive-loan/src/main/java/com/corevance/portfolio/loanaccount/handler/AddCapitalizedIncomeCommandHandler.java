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
package com.corevance.portfolio.loanaccount.handler;

import lombok.RequiredArgsConstructor;
import com.corevance.commands.annotation.CommandType;
import com.corevance.commands.handler.NewCommandSourceHandler;
import com.corevance.infrastructure.DataIntegrityErrorHandler;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.portfolio.loanaccount.service.CapitalizedIncomePlatformService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CommandType(entity = "LOAN", action = "CAPITALIZEDINCOME")
public class AddCapitalizedIncomeCommandHandler implements NewCommandSourceHandler {

    private final CapitalizedIncomePlatformService capitalizedIncomePlatformService;
    private final DataIntegrityErrorHandler dataIntegrityErrorHandler;

    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        try {
            return this.capitalizedIncomePlatformService.addCapitalizedIncome(command.getLoanId(), command);
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            dataIntegrityErrorHandler.handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve, "loan.capitalized.income",
                    "Capitalized Income");
            return CommandProcessingResult.empty();
        }
    }
}
