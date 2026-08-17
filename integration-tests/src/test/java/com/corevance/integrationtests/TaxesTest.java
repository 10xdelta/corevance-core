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
package com.corevance.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.corevance.client.feign.util.CallFailedRuntimeException;
import com.corevance.client.models.GetTaxesComponentsResponse;
import com.corevance.client.models.GetTaxesGroupResponse;
import com.corevance.client.models.GetTaxesGroupTaxAssociations;
import com.corevance.client.models.PostTaxesComponentsRequest;
import com.corevance.client.models.PostTaxesComponentsResponse;
import com.corevance.client.models.PostTaxesGroupRequest;
import com.corevance.client.models.PostTaxesGroupResponse;
import com.corevance.client.models.PostTaxesGroupTaxComponents;
import com.corevance.integrationtests.client.feign.helpers.FeignTaxComponentHelper;
import com.corevance.integrationtests.client.feign.helpers.FeignTaxGroupHelper;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;
import com.corevance.integrationtests.common.Utils;
import com.corevance.integrationtests.common.accounting.Account;
import com.corevance.integrationtests.common.accounting.AccountHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaxesTest {

    private FeignTaxComponentHelper taxComponentHelper;
    private FeignTaxGroupHelper taxGroupHelper;

    @BeforeEach
    public void setup() {
        taxComponentHelper = new FeignTaxComponentHelper(CorevanceFeignClientHelper.getCorevanceFeignClient());
        taxGroupHelper = new FeignTaxGroupHelper(CorevanceFeignClientHelper.getCorevanceFeignClient());
    }

    @Test
    public void createTaxComponentTest() {
        Long taxComponentId = createTaxComponentWithLiabilityToCredit("taxComponent");

        GetTaxesComponentsResponse taxComponentDetails = taxComponentHelper.retrieveTaxComponent(taxComponentId);
        Assertions.assertNotNull(taxComponentDetails);
        Assertions.assertNotNull(taxComponentDetails.getId());
        Assertions.assertEquals(taxComponentId, taxComponentDetails.getId());

        taxComponentId = createTaxComponentWithLiabilityToDebit("taxComponent");

        taxComponentDetails = taxComponentHelper.retrieveTaxComponent(taxComponentId);
        Assertions.assertNotNull(taxComponentDetails);
        Assertions.assertNotNull(taxComponentDetails.getId());
        Assertions.assertEquals(taxComponentId, taxComponentDetails.getId());
    }

    @Test
    public void createTaxGroupTest() {
        List<GetTaxesGroupResponse> allTaxGroups = taxGroupHelper.retrieveAllTaxGroups();
        Assertions.assertNotNull(allTaxGroups);

        final Long taxComponentId = createTaxComponentWithLiabilityToCredit("taxComponent");

        final Set<PostTaxesGroupTaxComponents> taxComponentsSet = new HashSet<>();
        taxComponentsSet.add(new PostTaxesGroupTaxComponents().taxComponentId(taxComponentId).startDate("01 January 2023"));
        final PostTaxesGroupRequest taxGroupRequest = new PostTaxesGroupRequest().name(Utils.randomStringGenerator("TAX_GRP_", 4))
                .taxComponents(taxComponentsSet).dateFormat("dd MMMM yyyy").locale("en");
        final PostTaxesGroupResponse taxGroupResponse = taxGroupHelper.createTaxGroup(taxGroupRequest);
        Assertions.assertNotNull(taxGroupResponse);
        Assertions.assertNotNull(taxGroupResponse.getResourceId());

        final GetTaxesGroupResponse taxGroupDetails = taxGroupHelper.retrieveTaxGroup(taxGroupResponse.getResourceId());
        Assertions.assertNotNull(taxGroupDetails);
        Assertions.assertEquals(taxGroupResponse.getResourceId(), taxGroupDetails.getId());
        Assertions.assertFalse(taxGroupDetails.getTaxAssociations().isEmpty());
        GetTaxesGroupTaxAssociations taxAssociation = taxGroupDetails.getTaxAssociations().iterator().next();
        Assertions.assertNotNull(taxAssociation);
        Assertions.assertEquals(taxComponentId, taxAssociation.getTaxComponent().getId());

        allTaxGroups = taxGroupHelper.retrieveAllTaxGroups();
        Assertions.assertNotNull(allTaxGroups);
        Assertions.assertTrue(allTaxGroups.size() > 0);
    }

    private Long createTaxComponentWithLiabilityToCredit(final String taxComponentPrefix) {
        final Account taxComponentGlAccount = AccountHelper.createLiabilityGlAccount(taxComponentPrefix);

        final PostTaxesComponentsRequest taxComponentRequest = new PostTaxesComponentsRequest()
                .name(Utils.randomStringGenerator(taxComponentPrefix, 4)).percentage(12.0f).startDate("01 January 2023")
                .creditAccountType(Integer.valueOf(taxComponentGlAccount.getAccountType().toString()))
                .creditAccountId(taxComponentGlAccount.getAccountID().longValue()).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);

        final PostTaxesComponentsResponse taxComponentRespose = taxComponentHelper.createTaxComponent(taxComponentRequest);
        Assertions.assertNotNull(taxComponentRespose);
        Assertions.assertNotNull(taxComponentRespose.getResourceId());

        return taxComponentRespose.getResourceId();
    }

    private Long createTaxComponentWithLiabilityToDebit(final String taxComponentPrefix) {
        final Account taxComponentGlAccount = AccountHelper.createLiabilityGlAccount(taxComponentPrefix);

        final PostTaxesComponentsRequest taxComponentRequest = new PostTaxesComponentsRequest()
                .name(Utils.randomStringGenerator(taxComponentPrefix, 4)).percentage(12.0f).startDate("01 January 2023")
                .debitAccountType(Integer.valueOf(taxComponentGlAccount.getAccountType().toString()))
                .debitAccountId(taxComponentGlAccount.getAccountID().longValue()).dateFormat(Utils.DATE_FORMAT).locale(Utils.LOCALE);

        final PostTaxesComponentsResponse taxComponentRespose = taxComponentHelper.createTaxComponent(taxComponentRequest);
        Assertions.assertNotNull(taxComponentRespose);
        Assertions.assertNotNull(taxComponentRespose.getResourceId());

        return taxComponentRespose.getResourceId();
    }

    @Test
    void retrieveTaxGroupWithNonExistentId_shouldReturn404() {
        final Long nonExistentTaxGroupId = 99999L;

        CallFailedRuntimeException exception = taxGroupHelper.retrieveTaxGroupExpectingError(nonExistentTaxGroupId);

        assertEquals(404, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.tax.group.id.invalid"),
                "Response should contain the error code for tax group not found");
    }

    @Test
    void retrieveTaxComponentWithNonExistentId_shouldReturn404() {
        final Long nonExistentTaxComponentId = 99999L;

        CallFailedRuntimeException exception = taxComponentHelper.retrieveTaxComponentExpectingError(nonExistentTaxComponentId);

        assertEquals(404, exception.getStatus());
        assertTrue(exception.getMessage().contains("error.msg.tax.component.id.invalid"),
                "Response should contain the error code for tax component not found");
    }

}
