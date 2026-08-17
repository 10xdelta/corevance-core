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
package com.corevance.portfolio.workingcapitalloanproduct.mapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.corevance.infrastructure.core.config.MapstructMapperConfig;
import com.corevance.infrastructure.core.data.StringEnumOptionData;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.organisation.monetary.data.CurrencyData;
import com.corevance.organisation.monetary.domain.MonetaryCurrency;
import com.corevance.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import com.corevance.portfolio.workingcapitalloanbreach.mapper.WorkingCapitalBreachMapper;
import com.corevance.portfolio.workingcapitalloannearbreach.mapper.WorkingCapitalNearBreachMapper;
import com.corevance.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductConfigurableAttributesData;
import com.corevance.portfolio.workingcapitalloanproduct.data.WorkingCapitalLoanProductData;
import com.corevance.portfolio.workingcapitalloanproduct.data.WorkingCapitalPaymentAllocationData;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAccountingRuleType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanBreachStartType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanDelinquencyStartType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductConfigurableAttributes;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductPaymentAllocationRule;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalPaymentAllocationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class, uses = { DelinquencyBucketMapper.class, WorkingCapitalBreachMapper.class,
        WorkingCapitalNearBreachMapper.class })
public interface WorkingCapitalLoanProductMapper {

    @Mapping(target = "fundId", source = "fund.id")
    @Mapping(target = "fundName", source = "fund.name")
    @Mapping(target = "externalId", source = "externalId", qualifiedByName = "externalIdToString")
    @Mapping(target = "status", source = "closeDate", qualifiedByName = "productStatus")
    @Mapping(target = "currency", source = "currency", qualifiedByName = "monetaryCurrencyToCurrencyData")
    @Mapping(target = "amortizationType", source = "relatedDetail.amortizationType", qualifiedByName = "amortizationToStringEnumOptionData")
    @Mapping(target = "npvDayCount", source = "relatedDetail.npvDayCount")
    @Mapping(target = "paymentAllocation", source = "paymentAllocationRules", qualifiedByName = "paymentAllocationRulesToData")
    @Mapping(target = "minPrincipal", source = "minMaxConstraints.minPrincipal")
    @Mapping(target = "principal", source = "relatedDetail.principal")
    @Mapping(target = "maxPrincipal", source = "minMaxConstraints.maxPrincipal")
    @Mapping(target = "minPeriodPaymentRate", source = "minMaxConstraints.minPeriodPaymentRate")
    @Mapping(target = "periodPaymentRate", source = "relatedDetail.periodPaymentRate")
    @Mapping(target = "maxPeriodPaymentRate", source = "minMaxConstraints.maxPeriodPaymentRate")
    @Mapping(target = "discount", source = "relatedDetail.discount")
    @Mapping(target = "repaymentEvery", source = "relatedDetail.repaymentEvery")
    @Mapping(target = "repaymentFrequencyType", source = "relatedDetail.repaymentFrequencyType", qualifiedByName = "periodFrequencyTypeToStringEnumOptionData")
    @Mapping(target = "breach", source = "breach")
    @Mapping(target = "nearBreach", source = "nearBreach")
    @Mapping(target = "allowAttributeOverrides", source = "configurableAttributes", qualifiedByName = "configurableAttributesToData")
    @Mapping(target = "delinquencyGraceDays", source = "relatedDetail.delinquencyGraceDays")
    @Mapping(target = "delinquencyStartType", source = "relatedDetail.delinquencyStartType", qualifiedByName = "delinquencyStartTypeToStringEnumOptionData")
    @Mapping(target = "breachGraceDays", source = "relatedDetail.breachGraceDays")
    @Mapping(target = "breachStartType", source = "relatedDetail.breachStartType", qualifiedByName = "breachStartTypeToStringEnumOptionData")
    @Mapping(target = "accountingRule", source = "accountingRule", qualifiedByName = "accountingRuleToStringEnumOptionData")
    @Mapping(target = "accountingMappings", ignore = true)
    @Mapping(target = "paymentChannelToFundSourceMappings", ignore = true)
    @Mapping(target = "feeToIncomeAccountMappings", ignore = true)
    @Mapping(target = "penaltyToIncomeAccountMappings", ignore = true)
    @Mapping(target = "chargeOffReasonToExpenseAccountMappings", ignore = true)
    @Mapping(target = "writeOffReasonsToExpenseMappings", ignore = true)
    @Mapping(target = "accountingRuleOptions", ignore = true)
    @Mapping(target = "accountingMappingOptions", ignore = true)
    @Mapping(target = "fundOptions", ignore = true)
    @Mapping(target = "paymentTypeOptions", ignore = true)
    @Mapping(target = "chargeOptions", ignore = true)
    @Mapping(target = "penaltyOptions", ignore = true)
    @Mapping(target = "currencyOptions", ignore = true)
    @Mapping(target = "amortizationTypeOptions", ignore = true)
    @Mapping(target = "periodFrequencyTypeOptions", ignore = true)
    @Mapping(target = "breachOptions", ignore = true)
    @Mapping(target = "advancedPaymentAllocationTypes", ignore = true)
    @Mapping(target = "advancedPaymentAllocationTransactionTypes", ignore = true)
    @Mapping(target = "applyTemplate", ignore = true)
    @Mapping(target = "delinquencyBucketOptions", ignore = true)
    @Mapping(target = "delinquencyStartTypeOptions", ignore = true)
    @Mapping(target = "breachStartTypeOptions", ignore = true)
    @Mapping(target = "delinquencyMinimumPaymentTypeOptions", ignore = true)
    @Mapping(target = "nearBreachOptions", ignore = true)
    @Mapping(target = "chargeOffReasonOptions", ignore = true)
    @Mapping(target = "writeOffReasonOptions", ignore = true)
    WorkingCapitalLoanProductData toData(WorkingCapitalLoanProduct entity);

    List<WorkingCapitalLoanProductData> toDataList(List<WorkingCapitalLoanProduct> entities);

    @Named("externalIdToString")
    default String externalIdToString(final ExternalId externalId) {
        return externalId != null ? externalId.getValue() : null;
    }

    @Named("productStatus")
    default String productStatus(final LocalDate closeDate) {
        return (closeDate != null && DateUtils.isBeforeBusinessDate(closeDate)) ? "loanProduct.inActive" : "loanProduct.active";
    }

    @Named("monetaryCurrencyToCurrencyData")
    default CurrencyData monetaryCurrencyToCurrencyData(final MonetaryCurrency currency) {
        if (currency == null) {
            return null;
        }
        return new CurrencyData(currency.getCode(), null, currency.getDigitsAfterDecimal(), currency.getInMultiplesOf(), null, null);
    }

    @Named("amortizationToStringEnumOptionData")
    default StringEnumOptionData amortizationToStringEnumOptionData(final WorkingCapitalAmortizationType amortizationType) {
        return amortizationType != null ? amortizationType.getValueAsStringEnumOptionData() : null;
    }

    @Named("periodFrequencyTypeToStringEnumOptionData")
    default StringEnumOptionData periodFrequencyTypeToStringEnumOptionData(
            final WorkingCapitalLoanPeriodFrequencyType periodFrequencyType) {
        return periodFrequencyType != null ? periodFrequencyType.getValueAsStringEnumOptionData() : null;
    }

    @Named("delinquencyStartTypeToStringEnumOptionData")
    default StringEnumOptionData delinquencyStartTypeToStringEnumOptionData(
            final WorkingCapitalLoanDelinquencyStartType delinquencyStartType) {
        return delinquencyStartType != null ? delinquencyStartType.getValueAsStringEnumOptionData() : null;
    }

    @Named("breachStartTypeToStringEnumOptionData")
    default StringEnumOptionData breachStartTypeToStringEnumOptionData(final WorkingCapitalLoanBreachStartType breachStartType) {
        return breachStartType != null ? breachStartType.getValueAsStringEnumOptionData() : null;
    }

    @Named("paymentAllocationRulesToData")
    default List<WorkingCapitalPaymentAllocationData> paymentAllocationRulesToData(
            final List<WorkingCapitalLoanProductPaymentAllocationRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        return rules.stream().map(rule -> {
            final List<WorkingCapitalPaymentAllocationData.PaymentAllocationOrder> paymentAllocationOrder = new ArrayList<>();
            final AtomicInteger counter = new AtomicInteger(1);
            for (final WorkingCapitalPaymentAllocationType allocationType : rule.getAllocationTypes()) {
                paymentAllocationOrder.add(
                        new WorkingCapitalPaymentAllocationData.PaymentAllocationOrder(allocationType.name(), counter.getAndIncrement()));
            }
            return new WorkingCapitalPaymentAllocationData(rule.getTransactionType() != null ? rule.getTransactionType() : null,
                    paymentAllocationOrder);
        }).toList();
    }

    @Named("accountingRuleToStringEnumOptionData")
    default StringEnumOptionData accountingRuleToStringEnumOptionData(final WorkingCapitalAccountingRuleType accountingRule) {
        if (accountingRule == null) {
            return null;
        }
        return accountingRule.toData();
    }

    @Named("configurableAttributesToData")
    default WorkingCapitalLoanProductConfigurableAttributesData configurableAttributesToData(
            final WorkingCapitalLoanProductConfigurableAttributes configurableAttributes) {
        if (configurableAttributes == null) {
            return null;
        }
        return WorkingCapitalLoanProductConfigurableAttributesData.builder() //
                .delinquencyBucketClassification(configurableAttributes.isDelinquencyBucketClassification()) //
                .breach(configurableAttributes.isBreach()) //
                .discountDefault(configurableAttributes.isDiscountDefaultOverridable()) //
                .periodPaymentFrequency(configurableAttributes.isPeriodPaymentFrequency()) //
                .periodPaymentFrequencyType(configurableAttributes.isPeriodPaymentFrequencyType()) //
                .build();
    }
}
