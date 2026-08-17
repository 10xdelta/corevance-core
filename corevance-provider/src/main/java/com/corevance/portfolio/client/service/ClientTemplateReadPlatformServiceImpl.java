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
package com.corevance.portfolio.client.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.codes.data.CodeValueData;
import com.corevance.infrastructure.codes.service.CodeValueReadPlatformService;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.data.EnumOptionData;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.infrastructure.dataqueries.data.DatatableData;
import com.corevance.infrastructure.dataqueries.data.EntityTables;
import com.corevance.infrastructure.dataqueries.data.StatusEnum;
import com.corevance.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.organisation.office.data.OfficeData;
import com.corevance.organisation.office.service.OfficeReadPlatformService;
import com.corevance.organisation.staff.data.StaffData;
import com.corevance.organisation.staff.service.StaffReadService;
import com.corevance.portfolio.address.data.AddressData;
import com.corevance.portfolio.address.service.AddressReadPlatformService;
import com.corevance.portfolio.client.api.ClientApiConstants;
import com.corevance.portfolio.client.data.ClientData;
import com.corevance.portfolio.client.data.ClientFamilyMembersData;
import com.corevance.portfolio.client.domain.ClientEnumerations;
import com.corevance.portfolio.client.domain.LegalForm;
import com.corevance.portfolio.savings.data.SavingsProductData;
import com.corevance.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class ClientTemplateReadPlatformServiceImpl implements ClientTemplateReadPlatformService {

    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final StaffReadService staffReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    // data mappers
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;

    private final AddressReadPlatformService addressReadPlatformService;
    private final ClientFamilyMembersReadPlatformService clientFamilyMembersReadPlatformService;
    private final ConfigurationDomainService configurationDomainService;

    @Override
    public ClientData retrieveTemplate(final Long officeId, final boolean staffInSelectedOfficeOnly) {
        this.context.authenticatedUser();

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);
        AddressData address = null;

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        final Collection<SavingsProductData> savingsProductDatas = this.savingsProductReadPlatformService.retrieveAllForLookupByType(null);

        final Boolean isAddressEnabled = configurationDomainService.isAddressEnabled();
        if (isAddressEnabled) {
            address = this.addressReadPlatformService.retrieveTemplate();
        }

        final ClientFamilyMembersData familyMemberOptions = this.clientFamilyMembersReadPlatformService.retrieveTemplate();

        Collection<StaffData> staffOptions = null;

        final boolean loanOfficersOnly = false;
        if (staffInSelectedOfficeOnly) {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
        } else {
            staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(defaultOfficeId,
                    loanOfficersOnly);
        }
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }
        final List<CodeValueData> genderOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER));

        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_TYPE));

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));

        final List<CodeValueData> clientNonPersonConstitutionOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION));

        final List<CodeValueData> clientNonPersonMainBusinessLineOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE));

        final List<EnumOptionData> clientLegalFormOptions = ClientEnumerations.legalForm(LegalForm.values());

        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService.retrieveTemplates(StatusEnum.CREATE.getValue(),
                EntityTables.CLIENT.getName(), null);

        return ClientData.template(defaultOfficeId, LocalDate.now(DateUtils.getDateTimeZoneOfTenant()), offices, staffOptions, null,
                genderOptions, savingsProductDatas, clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions,
                clientNonPersonMainBusinessLineOptions, clientLegalFormOptions, familyMemberOptions,
                new ArrayList<AddressData>(Arrays.asList(address)), isAddressEnabled, datatableTemplates);
    }

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

}
