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
package com.corevance.portfolio.loanorigination.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.portfolio.loanorigination.data.LoanOriginatorData;
import com.corevance.portfolio.loanorigination.domain.LoanOriginator;
import com.corevance.portfolio.loanorigination.domain.LoanOriginatorRepository;
import com.corevance.portfolio.loanorigination.domain.WorkingCapitalLoanOriginatorMapping;
import com.corevance.portfolio.loanorigination.domain.WorkingCapitalLoanOriginatorMappingRepository;
import com.corevance.portfolio.loanorigination.exception.LoanOriginatorNotFoundException;
import com.corevance.portfolio.loanorigination.mapper.LoanOriginatorMapper;
import com.corevance.portfolio.workingcapitalloan.service.WorkingCapitalLoanOriginatorReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(value = "corevance.module.loan-origination.enabled", havingValue = "true")
public class WorkingCapitalLoanOriginatorReadPlatformServiceImpl implements WorkingCapitalLoanOriginatorReadPlatformService {

    private final WorkingCapitalLoanOriginatorMappingRepository loanOriginatorMappingRepository;
    private final LoanOriginatorRepository loanOriginatorRepository;
    private final LoanOriginatorMapper loanOriginatorMapper;

    @Override
    public List<LoanOriginatorData> retrieveByLoanId(final Long loanId) {
        final List<WorkingCapitalLoanOriginatorMapping> mappings = this.loanOriginatorMappingRepository.findByLoanIdWithOriginator(loanId);
        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }
        return mappings.stream().map(WorkingCapitalLoanOriginatorMapping::getOriginator).map(this.loanOriginatorMapper::toData).toList();
    }

    @Override
    public Long resolveIdByExternalId(final String externalId) {
        final LoanOriginator originator = this.loanOriginatorRepository.findByExternalId(new ExternalId(externalId))
                .orElseThrow(() -> new LoanOriginatorNotFoundException(externalId));
        return originator.getId();
    }
}
