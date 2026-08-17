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
import com.corevance.infrastructure.codes.data.CodeValueData;
import com.corevance.infrastructure.codes.domain.CodeValueRepository;
import com.corevance.infrastructure.codes.mapper.CodeValueMapper;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.portfolio.loanorigination.api.LoanOriginatorApiConstants;
import com.corevance.portfolio.loanorigination.data.LoanOriginatorData;
import com.corevance.portfolio.loanorigination.data.LoanOriginatorTemplateData;
import com.corevance.portfolio.loanorigination.domain.LoanOriginator;
import com.corevance.portfolio.loanorigination.domain.LoanOriginatorMapping;
import com.corevance.portfolio.loanorigination.domain.LoanOriginatorMappingRepository;
import com.corevance.portfolio.loanorigination.domain.LoanOriginatorRepository;
import com.corevance.portfolio.loanorigination.domain.LoanOriginatorStatus;
import com.corevance.portfolio.loanorigination.exception.LoanOriginatorNotFoundException;
import com.corevance.portfolio.loanorigination.mapper.LoanOriginatorMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(value = "corevance.module.loan-origination.enabled", havingValue = "true")
public class LoanOriginatorReadPlatformServiceImpl implements LoanOriginatorReadPlatformService {

    private final LoanOriginatorRepository loanOriginatorRepository;
    private final LoanOriginatorMappingRepository loanOriginatorMappingRepository;
    private final LoanOriginatorMapper loanOriginatorMapper;
    private final CodeValueRepository codeValueRepository;
    private final CodeValueMapper codeValueMapper;

    @Override
    public List<LoanOriginatorData> retrieveAll() {
        final List<LoanOriginator> originators = this.loanOriginatorRepository.findAllWithCodeValues();
        return this.loanOriginatorMapper.toDataList(originators);
    }

    @Override
    public LoanOriginatorData retrieveById(final Long id) {
        final LoanOriginator originator = this.loanOriginatorRepository.findByIdWithCodeValues(id)
                .orElseThrow(() -> new LoanOriginatorNotFoundException(id));
        return this.loanOriginatorMapper.toData(originator);
    }

    @Override
    public LoanOriginatorData retrieveByExternalId(final String externalId) {
        final LoanOriginator originator = this.loanOriginatorRepository.findByExternalIdWithCodeValues(new ExternalId(externalId))
                .orElseThrow(() -> new LoanOriginatorNotFoundException(externalId));
        return this.loanOriginatorMapper.toData(originator);
    }

    @Override
    public Long resolveIdByExternalId(final String externalId) {
        final LoanOriginator originator = this.loanOriginatorRepository.findByExternalId(new ExternalId(externalId))
                .orElseThrow(() -> new LoanOriginatorNotFoundException(externalId));
        return originator.getId();
    }

    @Override
    public List<LoanOriginatorData> retrieveByLoanId(final Long loanId) {
        final List<LoanOriginatorMapping> mappings = this.loanOriginatorMappingRepository.findByLoanIdWithOriginator(loanId);
        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }
        return mappings.stream().map(LoanOriginatorMapping::getOriginator).map(this.loanOriginatorMapper::toData).toList();
    }

    @Override
    public LoanOriginatorTemplateData retrieveTemplate() {
        final List<CodeValueData> originationTypeOptions = codeValueMapper
                .map(codeValueRepository.findByCodeName(LoanOriginatorApiConstants.ORIGINATOR_TYPE_CODE_NAME));
        final List<CodeValueData> channelTypeOptions = codeValueMapper
                .map(codeValueRepository.findByCodeName(LoanOriginatorApiConstants.CHANNEL_TYPE_CODE_NAME));
        return new LoanOriginatorTemplateData(ExternalId.generate().getValue(), LoanOriginatorStatus.getAllValues(), originationTypeOptions,
                channelTypeOptions);
    }
}
