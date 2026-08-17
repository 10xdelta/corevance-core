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
package com.corevance.infrastructure.entityaccess.service;

import java.time.LocalDate;
import com.corevance.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import com.corevance.infrastructure.codes.service.CodeValueReadPlatformService;
import com.corevance.infrastructure.configuration.api.GlobalConfigurationConstants;
import com.corevance.infrastructure.configuration.domain.GlobalConfigurationProperty;
import com.corevance.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityAccessType;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityRelation;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityRelationRepositoryWrapper;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityToEntityMapping;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityToEntityMappingRepository;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityType;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CorevanceEntityAccessUtil {

    private final PlatformSecurityContext context;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final CorevanceEntityAccessWriteService corevanceEntityAccessWriteService;
    private final CorevanceEntityAccessReadService corevanceEntityAccessReadService;
    private final CorevanceEntityRelationRepositoryWrapper corevanceEntityRelationRepositoryWrapper;
    private final CorevanceEntityToEntityMappingRepository corevanceEntityToEntityMappingRepository;

    @Autowired
    public CorevanceEntityAccessUtil(final PlatformSecurityContext context,
            final GlobalConfigurationRepositoryWrapper globalConfigurationRepository,
            final CorevanceEntityAccessWriteService corevanceEntityAccessWriteService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final CodeValueRepositoryWrapper codeValueRepository,
            final CorevanceEntityAccessReadService corevanceEntityAccessReadService,
            final CorevanceEntityRelationRepositoryWrapper corevanceEntityRelationRepositoryWrapper,
            final CorevanceEntityToEntityMappingRepository corevanceEntityToEntityMappingRepository) {
        this.context = context;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.corevanceEntityAccessWriteService = corevanceEntityAccessWriteService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.codeValueRepository = codeValueRepository;
        this.corevanceEntityAccessReadService = corevanceEntityAccessReadService;
        this.corevanceEntityRelationRepositoryWrapper = corevanceEntityRelationRepositoryWrapper;
        this.corevanceEntityToEntityMappingRepository = corevanceEntityToEntityMappingRepository;
    }

    @Transactional
    public void checkConfigurationAndAddProductResrictionsForUserOffice(final CorevanceEntityAccessType corevanceEntityAccessType,
            final Long productOrChargeId) {

        AppUser thisUser = this.context.authenticatedUser();

        // check if the office specific products are enabled. If yes, then save
        // this product or charge against a specific office
        // i.e. this product or charge is specific for this office.

        final GlobalConfigurationProperty property = this.globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED);
        if (property.isEnabled()) {
            // If this property is enabled, then Corevance need to restrict
            // access to this loan product to only the office of the current
            // user
            final GlobalConfigurationProperty restrictToUserOfficeProperty = this.globalConfigurationRepository
                    .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.RESTRICT_PRODUCTS_TO_USER_OFFICE);

            if (restrictToUserOfficeProperty.isEnabled()) {
                final Long officeId = thisUser.getOffice().getId();
                LocalDate startDateFormapping = null;
                LocalDate endDateFormapping = null;
                CorevanceEntityRelation corevanceEntityRelation = corevanceEntityRelationRepositoryWrapper
                        .findOneByCodeName(corevanceEntityAccessType.getStr());
                Long relId = corevanceEntityRelation.getId();
                final CorevanceEntityRelation mapId = this.corevanceEntityRelationRepositoryWrapper.findOneWithNotFoundDetection(relId);
                final CorevanceEntityToEntityMapping newMap = CorevanceEntityToEntityMapping.newMap(mapId, officeId, productOrChargeId,
                        startDateFormapping, endDateFormapping);
                this.corevanceEntityToEntityMappingRepository.save(newMap);
            }
        }

    }

    public String getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(CorevanceEntityType corevanceEntityType) {
        String inClause = "";

        final GlobalConfigurationProperty property = this.globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED);

        if (property.isEnabled()) {
            // Get 'SQL In Clause' for fetching only products/charges that are
            // relevant for current user's office
            if (corevanceEntityType.equals(CorevanceEntityType.SAVINGS_PRODUCT)) {
                inClause = corevanceEntityAccessReadService
                        .getSQLQueryInClauseIDList_ForSavingsProductsForOffice(this.context.authenticatedUser().getOffice().getId(), false);
            } else if (corevanceEntityType.equals(CorevanceEntityType.LOAN_PRODUCT)) {
                inClause = corevanceEntityAccessReadService
                        .getSQLQueryInClauseIDList_ForLoanProductsForOffice(this.context.authenticatedUser().getOffice().getId(), false);
            } else if (corevanceEntityType.equals(CorevanceEntityType.CHARGE)) {
                inClause = corevanceEntityAccessReadService
                        .getSQLQueryInClauseIDList_ForChargesForOffice(this.context.authenticatedUser().getOffice().getId(), false);
            }
        }
        return inClause;
    }

}
