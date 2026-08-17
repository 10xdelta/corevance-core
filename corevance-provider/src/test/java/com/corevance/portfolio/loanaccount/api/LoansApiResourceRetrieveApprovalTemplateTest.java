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
package com.corevance.portfolio.loanaccount.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.UriInfo;
import java.util.Optional;
import com.corevance.commands.service.PortfolioCommandSourceWritePlatformService;
import com.corevance.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import com.corevance.infrastructure.bulkimport.service.BulkImportWorkbookService;
import com.corevance.infrastructure.codes.service.CodeValueReadPlatformService;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.api.ApiRequestParameterHelper;
import com.corevance.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.infrastructure.security.service.SqlValidator;
import com.corevance.portfolio.account.service.AccountAssociationsReadPlatformService;
import com.corevance.portfolio.account.service.PortfolioAccountReadPlatformService;
import com.corevance.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import com.corevance.portfolio.calendar.service.CalendarReadPlatformService;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.client.service.ClientReadPlatformService;
import com.corevance.portfolio.collateralmanagement.service.LoanCollateralManagementReadService;
import com.corevance.portfolio.delinquency.service.DelinquencyReadPlatformService;
import com.corevance.portfolio.fund.service.FundReadPlatformService;
import com.corevance.portfolio.group.service.GroupReadPlatformService;
import com.corevance.portfolio.loanaccount.domain.LoanApprovedAmountHistoryRepository;
import com.corevance.portfolio.loanaccount.domain.LoanSummaryBalancesRepository;
import com.corevance.portfolio.loanaccount.exception.NotSupportedLoanTemplateTypeException;
import com.corevance.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import com.corevance.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import com.corevance.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
import com.corevance.portfolio.loanaccount.repository.LoanCapitalizedIncomeBalanceRepository;
import com.corevance.portfolio.loanaccount.rescheduleloan.domain.LoanTermVariationsRepository;
import com.corevance.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import com.corevance.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import com.corevance.portfolio.loanaccount.service.LoanReadPlatformService;
import com.corevance.portfolio.loanaccount.service.LoanSummaryProviderDelegate;
import com.corevance.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import com.corevance.portfolio.loanproduct.service.LoanProductReadPlatformService;
import com.corevance.portfolio.note.service.NoteReadPlatformService;
import com.corevance.portfolio.rate.service.RateReadService;
import com.corevance.useradministration.domain.AppUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Exercises the <strong>public</strong> API
 * {@link LoansApiResource#retrieveApprovalTemplate(Long, String, jakarta.ws.rs.core.UriInfo)} — the HTTP-facing method
 * with parameters {@code (loanId, templateType, uriInfo)}. That method forwards to a <strong>private</strong> overload
 * {@code retrieveApprovalTemplate(Long, String, String, UriInfo)} which adds {@code loanExternalIdStr} (always
 * {@code null} for this entry point).
 * <p>
 * Only {@link PlatformSecurityContext} and {@link LoanReadPlatformService} are Mockito mocks under test control; other
 * constructor dependencies are plain {@code mock(...)} placeholders (same pattern as) . Each {@code import} in this
 * file matches a concrete type passed to {@code mock(...)} in {@link #newLoansApiResource}; all are required for
 * compilation (no looser shared type for those dependencies).
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LoansApiResourceRetrieveApprovalTemplateTest {

    /**
     * Must match the permission resource name used inside {@link LoansApiResource} for loan read operations (field
     * {@code RESOURCE_NAME_FOR_PERMISSIONS}, value {@code "LOAN"}). Corevance does not expose that field publicly; keep
     * this in sync if the production constant ever changes.
     */
    private static final String LOAN_READ_PERMISSION_RESOURCE = "LOAN";

    @Mock
    private PlatformSecurityContext context;
    @Mock
    private LoanReadPlatformService loanReadPlatformService;
    @Mock
    private AppUser appUser;

    private LoansApiResource loansApiResource;

    @BeforeEach
    void setUp() {
        loansApiResource = newLoansApiResource(context, loanReadPlatformService);
        when(context.authenticatedUser()).thenReturn(appUser);
        doNothing().when(appUser).validateHasReadPermission(LOAN_READ_PERMISSION_RESOURCE);
    }

    @Test
    void retrieveApprovalTemplate_unsupportedTemplateType_throwsNotSupportedLoanTemplateTypeException() {

        UriInfo uriInfo = mock(UriInfo.class);

        assertThrows(NotSupportedLoanTemplateTypeException.class,
                () -> loansApiResource.retrieveApprovalTemplate(1L, "unsupportedType", uriInfo));

        verify(appUser).validateHasReadPermission(LOAN_READ_PERMISSION_RESOURCE);
    }

    private static LoansApiResource newLoansApiResource(PlatformSecurityContext context, LoanReadPlatformService loanReadPlatformService) {

        ApiRequestParameterHelper helper = mock(ApiRequestParameterHelper.class);

        return new LoansApiResource(context, loanReadPlatformService, mock(LoanProductReadPlatformService.class),
                mock(LoanDropdownReadPlatformService.class), mock(FundReadPlatformService.class), mock(ChargeReadPlatformService.class),
                mock(LoanChargeReadPlatformService.class), mock(LoanScheduleCalculationPlatformService.class),
                mock(GuarantorReadPlatformService.class), mock(CodeValueReadPlatformService.class), mock(GroupReadPlatformService.class),
                mock(DefaultToApiJsonSerializer.class), mock(DefaultToApiJsonSerializer.class), mock(DefaultToApiJsonSerializer.class),
                mock(DefaultToApiJsonSerializer.class), helper, mock(FromJsonHelper.class),
                mock(PortfolioCommandSourceWritePlatformService.class), mock(CalendarReadPlatformService.class),
                mock(NoteReadPlatformService.class), mock(PortfolioAccountReadPlatformService.class),
                mock(AccountAssociationsReadPlatformService.class), mock(LoanScheduleHistoryReadPlatformService.class),
                mock(AccountDetailsReadPlatformService.class), mock(EntityDatatableChecksReadService.class),
                mock(BulkImportWorkbookService.class), mock(BulkImportWorkbookPopulatorService.class), mock(RateReadService.class),
                mock(ConfigurationDomainService.class), mock(DefaultToApiJsonSerializer.class),
                mock(GLIMAccountInfoReadPlatformService.class), mock(LoanCollateralManagementReadService.class),
                mock(DefaultToApiJsonSerializer.class), mock(DelinquencyReadPlatformService.class), mock(SqlValidator.class),
                mock(LoanSummaryBalancesRepository.class), mock(ClientReadPlatformService.class), mock(LoanTermVariationsRepository.class),
                mock(LoanSummaryProviderDelegate.class), mock(LoanCapitalizedIncomeBalanceRepository.class),
                mock(LoanApprovedAmountHistoryRepository.class), Optional.empty());
    }
}
