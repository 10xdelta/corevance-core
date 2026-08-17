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
package com.corevance.infrastructure.entityaccess.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import com.corevance.infrastructure.core.data.ApiParameterError;
import com.corevance.infrastructure.core.data.DataValidatorBuilder;
import com.corevance.infrastructure.core.exception.InvalidJsonException;
import com.corevance.infrastructure.core.exception.PlatformApiDataValidationException;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.entityaccess.api.CorevanceEntityApiResourceConstants;
import com.corevance.organisation.office.domain.OfficeRepositoryWrapper;
import com.corevance.portfolio.charge.domain.ChargeRepositoryWrapper;
import com.corevance.portfolio.loanproduct.domain.LoanProductRepository;
import com.corevance.portfolio.loanproduct.exception.LoanProductNotFoundException;
import com.corevance.portfolio.savings.domain.SavingsProductRepository;
import com.corevance.portfolio.savings.exception.SavingsProductNotFoundException;
import com.corevance.useradministration.domain.RoleRepository;
import com.corevance.useradministration.exception.RoleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CorevanceEntityDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final LoanProductRepository loanProductRepository;
    private final SavingsProductRepository savingsProductRepository;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;
    private final RoleRepository roleRepository;
    private static final Set<String> CREATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(CorevanceEntityApiResourceConstants.fromEnityType, CorevanceEntityApiResourceConstants.toEntityType,
                    CorevanceEntityApiResourceConstants.startDate, CorevanceEntityApiResourceConstants.LOCALE,
                    CorevanceEntityApiResourceConstants.DATE_FORMAT, CorevanceEntityApiResourceConstants.endDate));

    private static final Set<String> UPDATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(CorevanceEntityApiResourceConstants.relId, CorevanceEntityApiResourceConstants.fromEnityType,
                    CorevanceEntityApiResourceConstants.toEntityType, CorevanceEntityApiResourceConstants.startDate,
                    CorevanceEntityApiResourceConstants.LOCALE, CorevanceEntityApiResourceConstants.DATE_FORMAT,
                    CorevanceEntityApiResourceConstants.endDate));

    @Autowired
    public CorevanceEntityDataValidator(final FromJsonHelper fromApiJsonHelper, final OfficeRepositoryWrapper officeRepositoryWrapper,
            final LoanProductRepository loanProductRepository, final SavingsProductRepository savingsProductRepository,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final RoleRepository roleRepository) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.loanProductRepository = loanProductRepository;
        this.savingsProductRepository = savingsProductRepository;
        this.chargeRepositoryWrapper = chargeRepositoryWrapper;
        this.roleRepository = roleRepository;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(CorevanceEntityApiResourceConstants.COREVANCE_ENTITY_RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.fromEnityType, element)) {
            final Long fromId = this.fromApiJsonHelper.extractLongNamed(CorevanceEntityApiResourceConstants.fromEnityType, element);
            baseDataValidator.reset().parameter(CorevanceEntityApiResourceConstants.fromEnityType).value(fromId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.toEntityType, element)) {
            final Long toId = this.fromApiJsonHelper.extractLongNamed(CorevanceEntityApiResourceConstants.toEntityType, element);
            baseDataValidator.reset().parameter(CorevanceEntityApiResourceConstants.toEntityType).value(toId).notNull()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.startDate, element)) {
            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(CorevanceEntityApiResourceConstants.startDate, element);
            baseDataValidator.reset().parameter(CorevanceEntityApiResourceConstants.startDate).value(startDate);
        }

        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.endDate, element)) {
            final LocalDate endDate = this.fromApiJsonHelper.extractLocalDateNamed(CorevanceEntityApiResourceConstants.endDate, element);
            baseDataValidator.reset().parameter(CorevanceEntityApiResourceConstants.endDate).value(endDate);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void checkForEntity(String relId, Long fromId, Long toId) {

        switch (relId) {
            case "1":
                checkForOffice(fromId);
                checkForLoanProducts(toId);
            break;
            case "2":
                checkForOffice(fromId);
                checkForSavingsProducts(toId);
            break;
            case "3":
                checkForOffice(fromId);
                checkForCharges(toId);
            break;
            case "4":
                checkForRoles(fromId);
                checkForLoanProducts(toId);
            break;
            case "5":
                checkForRoles(fromId);
                checkForSavingsProducts(toId);
            break;

        }

    }

    public void checkForOffice(Long id) {
        this.officeRepositoryWrapper.findOneWithNotFoundDetection(id);
    }

    public void checkForLoanProducts(final Long id) {
        this.loanProductRepository.findById(id).orElseThrow(() -> new LoanProductNotFoundException(id));
    }

    public void checkForSavingsProducts(final Long id) {
        this.savingsProductRepository.findById(id).orElseThrow(() -> new SavingsProductNotFoundException(id));
    }

    public void checkForCharges(final Long id) {
        this.chargeRepositoryWrapper.findOneWithNotFoundDetection(id);
    }

    public void checkForRoles(final Long id) {
        this.roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_ENTITY_MAPPING_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(CorevanceEntityApiResourceConstants.COREVANCE_ENTITY_RESOURCE_NAME);

        boolean atLeastOneParameterPassedForUpdate = false;
        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.fromEnityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String fromEnityType = this.fromApiJsonHelper.extractStringNamed(CorevanceEntityApiResourceConstants.fromEnityType,
                    element);
            baseDataValidator.reset().parameter(CorevanceEntityApiResourceConstants.fromEnityType).value(fromEnityType);
        }

        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.fromEnityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String toEnityType = this.fromApiJsonHelper.extractStringNamed(CorevanceEntityApiResourceConstants.toEntityType, element);
            baseDataValidator.reset().parameter(CorevanceEntityApiResourceConstants.fromEnityType).value(toEnityType);
        }

        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.toEntityType, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.startDate, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (this.fromApiJsonHelper.parameterExists(CorevanceEntityApiResourceConstants.endDate, element)) {
            atLeastOneParameterPassedForUpdate = true;
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

}
