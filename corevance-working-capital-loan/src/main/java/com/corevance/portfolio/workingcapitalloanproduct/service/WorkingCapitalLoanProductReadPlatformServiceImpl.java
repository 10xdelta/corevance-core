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
package com.corevance.portfolio.workingcapitalloanproduct.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import com.corevance.accounting.common.AccountingDropdownReadPlatformService;
import com.corevance.accounting.glaccount.data.GLAccountData;
import com.corevance.accounting.producttoaccountmapping.service.WorkingCapitalLoanProductAdvancedAccountingReadHelper;
import com.corevance.infrastructure.codes.data.CodeValueData;
import com.corevance.infrastructure.codes.service.CodeValueReadPlatformService;
import com.corevance.infrastructure.core.api.ApiFacingEnum;
import com.corevance.infrastructure.core.data.EnumOptionData;
import com.corevance.infrastructure.core.data.StringEnumOptionData;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.organisation.monetary.data.CurrencyData;
import com.corevance.organisation.monetary.service.CurrencyReadPlatformService;
import com.corevance.portfolio.delinquency.data.DelinquencyBucketData;
import com.corevance.portfolio.delinquency.domain.DelinquencyMinimumPaymentType;
import com.corevance.portfolio.delinquency.service.DelinquencyReadPlatformService;
import com.corevance.portfolio.fund.data.FundData;
import com.corevance.portfolio.fund.service.FundReadPlatformService;
import com.corevance.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import com.corevance.portfolio.paymenttype.data.PaymentTypeData;
import com.corevance.portfolio.paymenttype.service.PaymentTypeReadService;
import com.corevance.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import com.corevance.portfolio.workingcapitalloanbreach.data.WorkingCapitalBreachData;
import com.corevance.portfolio.workingcapitalloanbreach.service.WorkingCapitalBreachReadPlatformService;
import com.corevance.portfolio.workingcapitalloannearbreach.data.WorkingCapitalNearBreachData;
import com.corevance.portfolio.workingcapitalloannearbreach.service.WorkingCapitalNearBreachReadPlatformService;
import com.corevance.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAccountingRuleType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanBreachStartType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanDelinquencyStartType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import com.corevance.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductNotFoundException;
import com.corevance.portfolio.workingcapitalloanproduct.mapper.WorkingCapitalLoanProductMapper;
import com.corevance.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkingCapitalLoanProductReadPlatformServiceImpl implements WorkingCapitalLoanProductReadPlatformService {

    private final WorkingCapitalLoanProductRepository repository;
    private final WorkingCapitalLoanProductMapper mapper;
    private final FundReadPlatformService fundReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;
    private final WorkingCapitalBreachReadPlatformService breachReadPlatformService;
    private final PaymentTypeReadService paymentTypeReadService;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final WorkingCapitalLoanProductAdvancedAccountingReadHelper advancedAccountingReadHelper;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final WorkingCapitalProductAccountingMappingService wcAccountingMappingService;
    private final WorkingCapitalNearBreachReadPlatformService nearBreachReadPlatformService;

    @Override
    public List<WorkingCapitalLoanProductData> retrieveAllWorkingCapitalLoanProducts() {
        final List<WorkingCapitalLoanProduct> products = this.repository.findAllWithDetails();
        return this.mapper.toDataList(products);
    }

    @Override
    public WorkingCapitalLoanProductData retrieveWorkingCapitalLoanProduct(final Long productId) {
        final WorkingCapitalLoanProduct product = this.repository.findByIdWithDetails(productId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));
        final WorkingCapitalLoanProductData productData = this.mapper.toData(product);

        if (product.getAccountingRule().isAccrualWithDeferredRevenueAmortization()) {
            final Map<String, GLAccountData> accountingMappings = this.wcAccountingMappingService.fetchAccountMappingDetails(productId,
                    product.getAccountingRule());
            productData.setAccountingMappings(accountingMappings);
            productData.setPaymentChannelToFundSourceMappings(advancedAccountingReadHelper.fetchPaymentTypeToFundSourceMappings(productId));
            productData.setFeeToIncomeAccountMappings(advancedAccountingReadHelper.fetchFeeToIncomeMappings(productId));
            productData.setPenaltyToIncomeAccountMappings(advancedAccountingReadHelper.fetchPenaltyToIncomeMappings(productId));
            productData.setChargeOffReasonToExpenseAccountMappings(advancedAccountingReadHelper.fetchChargeOffReasonMappings(productId));
            productData.setWriteOffReasonsToExpenseMappings(advancedAccountingReadHelper.fetchWriteOffReasonMappings(productId));
        }

        return productData;
    }

    @Override
    public WorkingCapitalLoanProduct retrieveWorkingCapitalLoanProductByExternalId(final ExternalId externalId) {
        return this.repository.findByExternalIdWithDetails(externalId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(externalId));
    }

    @Override
    public WorkingCapitalLoanProductData retrieveNewWorkingCapitalLoanProductDetails() {
        final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<StringEnumOptionData> amortizationTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalAmortizationType.class);
        final List<StringEnumOptionData> periodFrequencyTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanPeriodFrequencyType.class);
        final List<WorkingCapitalBreachData> breachOptions = breachReadPlatformService.retrieveAll();
        final List<StringEnumOptionData> advancedPaymentAllocationTypes = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalPaymentAllocationType.class);
        final List<StringEnumOptionData> delinquencyStartTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanDelinquencyStartType.class);
        final List<StringEnumOptionData> breachStartTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(WorkingCapitalLoanBreachStartType.class);
        final List<StringEnumOptionData> delinquencyMinimumPaymentTypeOptions = ApiFacingEnum
                .getValuesAsStringEnumOptionDataList(DelinquencyMinimumPaymentType.class);
        final List<EnumOptionData> advancedPaymentAllocationTransactionTypes = PaymentAllocationTransactionType
                .getValuesAsEnumOptionDataList();
        final Collection<DelinquencyBucketData> delinquencyBucketOptions = this.delinquencyReadPlatformService
                .retrieveAllDelinquencyBuckets();
        final List<WorkingCapitalNearBreachData> nearBreachOptions = nearBreachReadPlatformService.retrieveAll();
        final List<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadService.retrieveAllPaymentTypes();

        final List<StringEnumOptionData> accountingRuleOptions = WorkingCapitalAccountingRuleType.toStringEnumOptions();
        final Map<String, List<GLAccountData>> accountingMappingOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForLoanProducts();
        final List<CodeValueData> chargeOffReasonOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.CHARGE_OFF_REASONS);
        final List<CodeValueData> writeOffReasonOptions = this.codeValueReadPlatformService
                .retrieveCodeValuesByCode(WorkingCapitalLoanConstants.WRITE_OFF_REASONS);

        return WorkingCapitalLoanProductData.builder() //
                .fundOptions(fundOptions) //
                .currencyOptions(currencyOptions) //
                .amortizationTypeOptions(amortizationTypeOptions) //
                .periodFrequencyTypeOptions(periodFrequencyTypeOptions) //
                .breachOptions(breachOptions) //
                .advancedPaymentAllocationTypes(advancedPaymentAllocationTypes) //
                .advancedPaymentAllocationTransactionTypes(advancedPaymentAllocationTransactionTypes) //
                .delinquencyStartTypeOptions(delinquencyStartTypeOptions) //
                .breachStartTypeOptions(breachStartTypeOptions) //
                .delinquencyMinimumPaymentTypeOptions(delinquencyMinimumPaymentTypeOptions) //
                .delinquencyBucketOptions(
                        delinquencyBucketOptions != null && !delinquencyBucketOptions.isEmpty() ? delinquencyBucketOptions : null) //
                .paymentTypeOptions(paymentTypeOptions != null && !paymentTypeOptions.isEmpty() ? paymentTypeOptions : null) //
                // TODO: Populate WC-specific charge options when WC charges are introduced.
                .chargeOptions(List.of()) //
                .penaltyOptions(List.of()) //
                .accountingRuleOptions(accountingRuleOptions) //
                .accountingMappingOptions(accountingMappingOptions) //
                .nearBreachOptions(nearBreachOptions) //
                .chargeOffReasonOptions(chargeOffReasonOptions != null && !chargeOffReasonOptions.isEmpty() ? chargeOffReasonOptions : null) //
                .writeOffReasonOptions(writeOffReasonOptions != null && !writeOffReasonOptions.isEmpty() ? writeOffReasonOptions : null) //
                .build();
    }
}
