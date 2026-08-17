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
package com.corevance.portfolio.workingcapitalloan.service;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import com.corevance.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import com.corevance.infrastructure.accountnumberformat.domain.AccountNumberFormatLookup;
import com.corevance.infrastructure.accountnumberformat.domain.EntityAccountType;
import com.corevance.infrastructure.accountnumberformat.service.AccountNumberGeneratorService;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.domain.ExternalId;
import com.corevance.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.infrastructure.core.service.ExternalIdFactory;
import com.corevance.organisation.monetary.domain.MonetaryCurrency;
import com.corevance.portfolio.client.domain.Client;
import com.corevance.portfolio.client.domain.ClientRepository;
import com.corevance.portfolio.client.exception.ClientNotFoundException;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucket;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucketRepository;
import com.corevance.portfolio.fund.domain.Fund;
import com.corevance.portfolio.fund.domain.FundRepository;
import com.corevance.portfolio.fund.exception.FundNotFoundException;
import com.corevance.portfolio.loanaccount.domain.LoanStatus;
import com.corevance.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import com.corevance.portfolio.workingcapitalloan.WorkingCapitalLoanConstants;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanBalance;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanDisbursementDetails;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPaymentAllocationRule;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanPeriodFrequencyType;
import com.corevance.portfolio.workingcapitalloan.mapper.WorkingCapitalLoanPaymentAllocationMapper;
import com.corevance.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import com.corevance.portfolio.workingcapitalloanbreach.domain.WorkingCapitalBreach;
import com.corevance.portfolio.workingcapitalloanbreach.repository.WorkingCapitalBreachRepository;
import com.corevance.portfolio.workingcapitalloannearbreach.domain.WorkingCapitalNearBreach;
import com.corevance.portfolio.workingcapitalloannearbreach.repository.WorkingCapitalNearBreachRepository;
import com.corevance.portfolio.workingcapitalloanproduct.WorkingCapitalLoanProductConstants;
import com.corevance.portfolio.workingcapitalloanproduct.data.WorkingCapitalPaymentAllocationData;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAdvancedPaymentAllocationsJsonParser;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanBreachStartType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanDelinquencyStartType;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProduct;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductPaymentAllocationRule;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetail;
import com.corevance.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanProductRelatedDetails;
import com.corevance.portfolio.workingcapitalloanproduct.exception.WorkingCapitalLoanProductNotFoundException;
import com.corevance.portfolio.workingcapitalloanproduct.repository.WorkingCapitalLoanProductRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkingCapitalLoanAssemblerImpl implements WorkingCapitalLoanAssembler {

    private final FromJsonHelper fromApiJsonHelper;
    private final WorkingCapitalLoanProductRepository loanProductRepository;
    private final ClientRepository clientRepository;
    private final FundRepository fundRepository;
    private final DelinquencyBucketRepository delinquencyBucketRepository;
    private final ExternalIdFactory externalIdFactory;
    private final WorkingCapitalAdvancedPaymentAllocationsJsonParser paymentAllocationParser;
    private final AccountNumberFormatLookup accountNumberFormatLookup;
    private final AccountNumberGeneratorService accountNumberGeneratorService;
    private final WorkingCapitalLoanRepository workingCapitalLoanRepository;
    private final WorkingCapitalBreachRepository breachRepository;
    private final WorkingCapitalNearBreachRepository nearBreachRepository;
    private final WorkingCapitalLoanPaymentAllocationMapper workingCapitalLoanPaymentAllocationMapper;

    @Override
    public WorkingCapitalLoan assembleFrom(final JsonCommand command) {
        final JsonElement element = command.parsedJson();

        final Long clientId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.clientIdParameterName, element);
        final Client client = clientRepository.findById(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));

        final Long productId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.productIdParameterName, element);
        final WorkingCapitalLoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));

        final Long fundId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.fundIdParameterName, element);
        final Fund fund = fundId != null ? fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId)) : null;

        final String accountNo = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
        final String externalIdStr = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName, element);
        final ExternalId externalId = externalIdFactory.create(externalIdStr);

        final BigDecimal principal = fromApiJsonHelper
                .extractBigDecimalWithLocaleNamed(WorkingCapitalLoanConstants.principalAmountParamName, element);
        final BigDecimal totalPaymentVolume = fromApiJsonHelper
                .extractBigDecimalNamed(WorkingCapitalLoanConstants.totalPaymentVolumeParamName, element, new HashSet<>());

        final LocalDate submittedOnDate = fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.submittedOnDateParameterName,
                element) ? fromApiJsonHelper.extractLocalDateNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName, element)
                        : DateUtils.getBusinessLocalDate();
        final LocalDate expectedDisbursementDate = fromApiJsonHelper
                .extractLocalDateNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, element);

        final WorkingCapitalLoanProductRelatedDetails loanProductRelatedDetails = buildLoanProductRelatedDetails(element, product);

        final WorkingCapitalLoan loan = new WorkingCapitalLoan();
        loan.setAccountNumber(accountNo != null ? accountNo : "");
        loan.setExternalId(externalId);
        loan.setClient(client);
        loan.setFund(fund);
        loan.setLoanProduct(product);
        loan.setLoanStatus(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        final Integer currentMaxLoanProductCounter = workingCapitalLoanRepository
                .findMaxLoanProductCounterByClientAndProduct(client.getId(), product.getId());
        final int newLoanProductCounter = currentMaxLoanProductCounter == null ? 1 : currentMaxLoanProductCounter + 1;
        loan.setLoanProductCounter(newLoanProductCounter);
        loan.setLoanCounter(newLoanProductCounter);
        loan.setSubmittedOnDate(submittedOnDate);
        if (expectedDisbursementDate != null) {
            final WorkingCapitalLoanDisbursementDetails detail = new WorkingCapitalLoanDisbursementDetails();
            detail.setWcLoan(loan);
            detail.setExpectedDisbursementDate(expectedDisbursementDate);
            detail.setExpectedAmount(principal);
            loan.getDisbursementDetails().add(detail);
        }

        loan.setProposedPrincipal(principal);
        loan.setApprovedPrincipal(BigDecimal.ZERO);
        final WorkingCapitalLoanBalance balance = WorkingCapitalLoanBalance.createFor(loan);
        loan.setTotalPaymentVolume(totalPaymentVolume != null ? totalPaymentVolume : BigDecimal.ZERO);
        loan.setBalance(balance);
        loan.setLoanProductRelatedDetails(loanProductRelatedDetails);

        copyPaymentAllocationRules(loan, command, product);

        return loan;
    }

    private WorkingCapitalLoanProductRelatedDetails buildLoanProductRelatedDetails(final JsonElement element,
            final WorkingCapitalLoanProduct product) {
        final WorkingCapitalLoanProductRelatedDetail productDetail = product.getRelatedDetail();
        final MonetaryCurrency currency = product.getCurrency();

        final WorkingCapitalLoanProductRelatedDetails detail = new WorkingCapitalLoanProductRelatedDetails();
        detail.setCurrency(currency);
        detail.setPrincipal(fromApiJsonHelper.parameterExists(WorkingCapitalLoanConstants.principalAmountParamName, element)
                ? fromApiJsonHelper.extractBigDecimalWithLocaleNamed(WorkingCapitalLoanConstants.principalAmountParamName, element)
                : productDetail.getPrincipal());
        detail.setPeriodPaymentRate(
                fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element)
                        ? fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element,
                                new HashSet<>())
                        : productDetail.getPeriodPaymentRate());
        detail.setRepaymentEvery(fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element)
                ? fromApiJsonHelper.extractIntegerWithLocaleNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element)
                : productDetail.getRepaymentEvery());
        detail.setRepaymentFrequencyType(
                fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element)
                        ? WorkingCapitalLoanPeriodFrequencyType.valueOf(fromApiJsonHelper
                                .extractStringNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element))
                        : productDetail.getRepaymentFrequencyType());
        detail.setAmortizationType(productDetail.getAmortizationType());
        detail.setNpvDayCount(productDetail.getNpvDayCount());
        detail.setDiscountProposed(fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.discountParamName, element)
                ? fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.discountParamName, element, new HashSet<>())
                : null);
        if (detail.getDiscountProposed() == null && productDetail.getDiscount() != null
                && productDetail.getDiscount().compareTo(BigDecimal.ZERO) > 0
                && !product.getConfigurableAttributes().isDiscountDefaultOverridable()) {
            detail.setDiscountProposed(productDetail.getDiscount());
        }
        final Long breachId = fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.breachIdParamName, element)
                ? fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.breachIdParamName, element)
                : null;
        if (breachId != null) {
            detail.setBreach(findBreachById(breachId));
        } else {
            detail.setBreach(product.getBreach());
        }
        final Long nearBreachId = fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.nearBreachIdParamName, element)
                ? fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.nearBreachIdParamName, element)
                : null;
        if (nearBreachId != null) {
            detail.setNearBreach(findNearBreachById(nearBreachId));
        } else {
            detail.setNearBreach(product.getNearBreach());
        }
        detail.setDelinquencyGraceDays(
                fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyGraceDaysParamName, element)
                        ? fromApiJsonHelper.extractIntegerNamed(WorkingCapitalLoanProductConstants.delinquencyGraceDaysParamName, element,
                                new HashSet<>())
                        : productDetail.getDelinquencyGraceDays());
        detail.setBreachGraceDays(fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.breachGraceDaysParamName, element)
                ? fromApiJsonHelper.extractIntegerNamed(WorkingCapitalLoanProductConstants.breachGraceDaysParamName, element,
                        new HashSet<>())
                : productDetail.getBreachGraceDays());
        final String delinquencyStartTypeValue = fromApiJsonHelper
                .parameterExists(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName, element)
                        ? fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName, element)
                        : null;
        if (delinquencyStartTypeValue != null) {
            detail.setDelinquencyStartType(WorkingCapitalLoanDelinquencyStartType.fromString(delinquencyStartTypeValue));
        } else {
            detail.setDelinquencyStartType(productDetail.getDelinquencyStartType());
        }
        final String breachStartTypeValue = fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.breachStartTypeParamName,
                element) ? fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.breachStartTypeParamName, element)
                        : null;
        if (breachStartTypeValue != null) {
            detail.setBreachStartType(WorkingCapitalLoanBreachStartType.fromString(breachStartTypeValue));
        } else {
            detail.setBreachStartType(productDetail.getBreachStartType());
        }

        if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element)) {
            final Long bucketId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName,
                    element);
            detail.setDelinquencyBucket(bucketId != null ? delinquencyBucketRepository.findById(bucketId).orElse(null) : null);
        } else {
            detail.setDelinquencyBucket(product.getDelinquencyBucket());
        }

        return detail;
    }

    private List<WorkingCapitalPaymentAllocationData> copyPaymentAllocationRules(final WorkingCapitalLoan loan, final JsonCommand command,
            final WorkingCapitalLoanProduct product) {
        final List<WorkingCapitalLoanProductPaymentAllocationRule> rules;
        if (command.arrayOfParameterNamed(WorkingCapitalLoanProductConstants.paymentAllocationParamName) != null) {
            rules = paymentAllocationParser.assembleWCPaymentAllocationRules(command);
        } else {
            rules = product.getPaymentAllocationRules().stream().toList();
        }

        final Map<PaymentAllocationTransactionType, WorkingCapitalLoanPaymentAllocationRule> existingRulesByTransactionType = new HashMap<>();
        final HashSet<PaymentAllocationTransactionType> incomingTransactionTypes = new HashSet<>(
                rules.stream().map(WorkingCapitalLoanProductPaymentAllocationRule::getTransactionType).toList());

        loan.getPaymentAllocationRules().removeIf(existingRule -> {
            if (!incomingTransactionTypes.contains(existingRule.getTransactionType())) {
                return true;
            }
            return existingRulesByTransactionType.putIfAbsent(existingRule.getTransactionType(), existingRule) != null;
        });

        for (final WorkingCapitalLoanProductPaymentAllocationRule rule : rules) {
            final WorkingCapitalLoanPaymentAllocationRule existingRule = existingRulesByTransactionType.get(rule.getTransactionType());
            if (existingRule != null) {
                existingRule.setAllocationTypes(rule.getAllocationTypes());
            } else {
                final WorkingCapitalLoanPaymentAllocationRule newRule = new WorkingCapitalLoanPaymentAllocationRule(loan,
                        rule.getTransactionType(), rule.getAllocationTypes());
                loan.getPaymentAllocationRules().add(newRule);
                existingRulesByTransactionType.put(rule.getTransactionType(), newRule);
            }
        }
        return workingCapitalLoanPaymentAllocationMapper.paymentAllocationRulesToData(loan.getPaymentAllocationRules());
    }

    @Override
    public Map<String, Object> updateFrom(final JsonCommand command, final WorkingCapitalLoan loan) {
        final Map<String, Object> changes = new HashMap<>();
        final JsonElement element = command.parsedJson();

        if (command.isChangeInLongParameterNamed(WorkingCapitalLoanConstants.clientIdParameterName,
                loan.getClient() != null ? loan.getClient().getId() : null)) {
            final Long clientId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.clientIdParameterName, element);
            final Client client = clientRepository.findById(clientId).orElseThrow(() -> new ClientNotFoundException(clientId));
            loan.setClient(client);
            changes.put(WorkingCapitalLoanConstants.clientIdParameterName, clientId);
        }
        if (command.isChangeInLongParameterNamed(WorkingCapitalLoanConstants.productIdParameterName,
                loan.getLoanProduct() != null ? loan.getLoanProduct().getId() : null)) {
            final Long productId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.productIdParameterName, element);
            final WorkingCapitalLoanProduct product = loanProductRepository.findById(productId)
                    .orElseThrow(() -> new WorkingCapitalLoanProductNotFoundException(productId));
            loan.setLoanProduct(product);
            changes.put(WorkingCapitalLoanConstants.productIdParameterName, productId);
        }
        final Long existingFundId = loan.getFund() != null ? loan.getFund().getId() : null;
        if (command.isChangeInLongParameterNamed(WorkingCapitalLoanConstants.fundIdParameterName, existingFundId)) {
            final Long fundId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanConstants.fundIdParameterName, element);
            final Fund fund = fundId != null ? fundRepository.findById(fundId).orElseThrow(() -> new FundNotFoundException(fundId)) : null;
            loan.setFund(fund);
            changes.put(WorkingCapitalLoanConstants.fundIdParameterName, fundId);
        }
        if (command.isChangeInStringParameterNamed(WorkingCapitalLoanConstants.accountNoParameterName, loan.getAccountNumber())) {
            final String accountNo = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
            loan.setAccountNumber(accountNo != null ? accountNo : "");
            changes.put(WorkingCapitalLoanConstants.accountNoParameterName, loan.getAccountNumber());
        }
        if (command.parameterExists(WorkingCapitalLoanConstants.externalIdParameterName)) {
            final ExternalId existing = loan.getExternalId();
            final boolean changed = existing == null
                    ? command.stringValueOfParameterNamed(WorkingCapitalLoanConstants.externalIdParameterName) != null
                    : command.isChangeInExternalIdParameterNamed(WorkingCapitalLoanConstants.externalIdParameterName, existing);
            if (changed) {
                final String externalIdStr = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.externalIdParameterName,
                        element);
                loan.setExternalId(externalIdFactory.create(externalIdStr));
                changes.put(WorkingCapitalLoanConstants.externalIdParameterName, externalIdStr);
            }
        }
        final BigDecimal currentPrincipal = loan.getBalance() != null ? loan.getBalance().getPrincipalOutstanding() : null;
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanConstants.principalAmountParamName, currentPrincipal)) {
            final BigDecimal principal = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(WorkingCapitalLoanConstants.principalAmountParamName, element);
            loan.setProposedPrincipal(principal);
            loan.setApprovedPrincipal(BigDecimal.ZERO);
            changes.put(WorkingCapitalLoanConstants.principalAmountParamName, principal);
        }
        final BigDecimal currenttotalPaymentVolumeVolume = loan.getTotalPaymentVolume();
        if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanConstants.totalPaymentVolumeParamName,
                currenttotalPaymentVolumeVolume)) {
            final BigDecimal totalPaymentVolume = fromApiJsonHelper
                    .extractBigDecimalNamed(WorkingCapitalLoanConstants.totalPaymentVolumeParamName, element, new HashSet<>());
            loan.setTotalPaymentVolume(totalPaymentVolume);
            changes.put(WorkingCapitalLoanConstants.totalPaymentVolumeParamName, totalPaymentVolume);
        }
        if (command.isChangeInLocalDateParameterNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName,
                loan.getSubmittedOnDate())) {
            final LocalDate submittedOnDate = fromApiJsonHelper
                    .extractLocalDateNamed(WorkingCapitalLoanConstants.submittedOnDateParameterName, element);
            loan.setSubmittedOnDate(submittedOnDate);
            changes.put(WorkingCapitalLoanConstants.submittedOnDateParameterName, submittedOnDate);
        }
        final LocalDate currentExpectedDisbursementDate = loan.getDisbursementDetails().isEmpty() ? null
                : loan.getDisbursementDetails().get(0).getExpectedDisbursementDate();
        if (command.isChangeInLocalDateParameterNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName,
                currentExpectedDisbursementDate)) {
            final LocalDate expectedDisbursementDate = fromApiJsonHelper
                    .extractLocalDateNamed(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, element);
            if (!loan.getDisbursementDetails().isEmpty()) {
                loan.getDisbursementDetails().getFirst().setExpectedDisbursementDate(expectedDisbursementDate);
            } else if (expectedDisbursementDate != null) {
                final WorkingCapitalLoanDisbursementDetails detail = new WorkingCapitalLoanDisbursementDetails();
                detail.setWcLoan(loan);
                detail.setExpectedDisbursementDate(expectedDisbursementDate);
                loan.getDisbursementDetails().add(detail);
            }
            changes.put(WorkingCapitalLoanConstants.expectedDisbursementDateParameterName, expectedDisbursementDate);
        }

        final WorkingCapitalLoanProductRelatedDetails detail = loan.getLoanProductRelatedDetails();
        if (detail != null) {
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element)
                    && command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName,
                            detail.getPeriodPaymentRate())) {
                final BigDecimal periodPaymentRate = fromApiJsonHelper
                        .extractBigDecimalNamed(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, element, new HashSet<>());
                detail.setPeriodPaymentRate(periodPaymentRate);
                changes.put(WorkingCapitalLoanProductConstants.periodPaymentRateParamName, periodPaymentRate);
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element)
                    && command.isChangeInIntegerParameterNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName,
                            detail.getRepaymentEvery())) {
                final Integer repaymentEvery = fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(WorkingCapitalLoanProductConstants.repaymentEveryParamName, element);
                detail.setRepaymentEvery(repaymentEvery);
                changes.put(WorkingCapitalLoanProductConstants.repaymentEveryParamName, repaymentEvery);
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element)
                    && command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName,
                            detail.getRepaymentFrequencyType().name())) {
                final WorkingCapitalLoanPeriodFrequencyType type = WorkingCapitalLoanPeriodFrequencyType.valueOf(
                        fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, element));
                detail.setRepaymentFrequencyType(type);
                changes.put(WorkingCapitalLoanProductConstants.repaymentFrequencyTypeParamName, type.name());
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.discountParamName, element)) {
                final BigDecimal discount = fromApiJsonHelper.extractBigDecimalNamed(WorkingCapitalLoanProductConstants.discountParamName,
                        element, new HashSet<>());
                if (command.isChangeInBigDecimalParameterNamed(WorkingCapitalLoanProductConstants.discountParamName,
                        detail.getDiscount())) {
                    detail.setDiscountProposed(discount);
                    changes.put(WorkingCapitalLoanProductConstants.discountParamName, discount);
                }
            }
            final Long existingBreachId = detail.getBreach() != null ? detail.getBreach().getId() : null;
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.breachIdParamName, element)
                    && command.isChangeInLongParameterNamed(WorkingCapitalLoanProductConstants.breachIdParamName, existingBreachId)) {
                final Long breachId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.breachIdParamName, element);
                detail.setBreach(breachId != null ? findBreachById(breachId) : null);
                changes.put(WorkingCapitalLoanProductConstants.breachIdParamName, breachId);
            }
            final Long existingNearBreachId = detail.getNearBreach() != null ? detail.getNearBreach().getId() : null;
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.nearBreachIdParamName, element) && command
                    .isChangeInLongParameterNamed(WorkingCapitalLoanProductConstants.nearBreachIdParamName, existingNearBreachId)) {
                final Long nearBreachId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.nearBreachIdParamName,
                        element);
                detail.setNearBreach(nearBreachId != null ? findNearBreachById(nearBreachId) : null);
                changes.put(WorkingCapitalLoanProductConstants.nearBreachIdParamName, nearBreachId);
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, element)) {
                final Long bucketId = fromApiJsonHelper.extractLongNamed(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName,
                        element);
                final DelinquencyBucket bucket = bucketId != null ? delinquencyBucketRepository.findById(bucketId).orElse(null) : null;
                final Long existingBucketId = detail.getDelinquencyBucket() != null ? detail.getDelinquencyBucket().getId() : null;
                if (!Objects.equals(bucketId, existingBucketId)) {
                    detail.setDelinquencyBucket(bucket);
                    changes.put(WorkingCapitalLoanProductConstants.delinquencyBucketIdParamName, bucketId);
                }
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyGraceDaysParamName, element)) {
                final Integer delinquencyGraceDays = fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(WorkingCapitalLoanProductConstants.delinquencyGraceDaysParamName, element);
                if (command.isChangeInIntegerParameterNamed(WorkingCapitalLoanProductConstants.delinquencyGraceDaysParamName,
                        detail.getDelinquencyGraceDays())) {
                    detail.setDelinquencyGraceDays(delinquencyGraceDays);
                    changes.put(WorkingCapitalLoanProductConstants.delinquencyGraceDaysParamName, delinquencyGraceDays);
                }
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.breachGraceDaysParamName, element)) {
                final Integer breachGraceDays = fromApiJsonHelper
                        .extractIntegerWithLocaleNamed(WorkingCapitalLoanProductConstants.breachGraceDaysParamName, element);
                if (command.isChangeInIntegerParameterNamed(WorkingCapitalLoanProductConstants.breachGraceDaysParamName,
                        detail.getBreachGraceDays())) {
                    detail.setBreachGraceDays(breachGraceDays);
                    changes.put(WorkingCapitalLoanProductConstants.breachGraceDaysParamName, breachGraceDays);
                }
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName, element)) {
                final String existingValue = detail.getDelinquencyStartType() != null ? detail.getDelinquencyStartType().name() : null;
                if (command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName,
                        existingValue)) {
                    final String delinquencyStartTypeValue = fromApiJsonHelper
                            .extractStringNamed(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName, element);
                    if (delinquencyStartTypeValue != null) {
                        final WorkingCapitalLoanDelinquencyStartType type = WorkingCapitalLoanDelinquencyStartType
                                .fromString(delinquencyStartTypeValue);
                        detail.setDelinquencyStartType(type);
                        changes.put(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName, type.getCode());
                    } else {
                        detail.setDelinquencyStartType(null);
                        changes.put(WorkingCapitalLoanProductConstants.delinquencyStartTypeParamName, null);
                    }
                }
            }
            if (fromApiJsonHelper.parameterExists(WorkingCapitalLoanProductConstants.breachStartTypeParamName, element)) {
                final String existingValue = detail.getBreachStartType() != null ? detail.getBreachStartType().name() : null;
                if (command.isChangeInStringParameterNamed(WorkingCapitalLoanProductConstants.breachStartTypeParamName, existingValue)) {
                    final String breachStartTypeValue = fromApiJsonHelper
                            .extractStringNamed(WorkingCapitalLoanProductConstants.breachStartTypeParamName, element);
                    if (breachStartTypeValue != null) {
                        final WorkingCapitalLoanBreachStartType type = WorkingCapitalLoanBreachStartType.fromString(breachStartTypeValue);
                        detail.setBreachStartType(type);
                        changes.put(WorkingCapitalLoanProductConstants.breachStartTypeParamName, type.getCode());
                    } else {
                        detail.setBreachStartType(WorkingCapitalLoanBreachStartType.DISBURSEMENT);
                        changes.put(WorkingCapitalLoanProductConstants.breachStartTypeParamName,
                                WorkingCapitalLoanBreachStartType.DISBURSEMENT.getCode());
                    }
                }
            }
        }

        if (command.arrayOfParameterNamed(WorkingCapitalLoanProductConstants.paymentAllocationParamName) != null) {
            List<WorkingCapitalPaymentAllocationData> newPaymentAllocationRules = copyPaymentAllocationRules(loan, command,
                    loan.getLoanProduct());
            changes.put(WorkingCapitalLoanProductConstants.paymentAllocationParamName, newPaymentAllocationRules);
        }

        return changes;
    }

    /**
     * If accountNo was provided in the request, leave it. Otherwise generate via the same infrastructure as Loan
     * (AccountNumberFormat + AccountNumberGeneratorService for EntityAccountType.WORKING_CAPITAL_LOAN).
     */
    @Override
    public void accountNumberGeneration(final JsonCommand command, final WorkingCapitalLoan loan) {
        final JsonElement element = command.parsedJson();
        final String accountNo = fromApiJsonHelper.extractStringNamed(WorkingCapitalLoanConstants.accountNoParameterName, element);
        if (!StringUtils.isBlank(accountNo)) {
            return;
        }
        final AccountNumberFormat format = accountNumberFormatLookup.findByAccountType(EntityAccountType.WORKING_CAPITAL_LOAN);
        final String generated = accountNumberGeneratorService.generate(EntityAccountType.WORKING_CAPITAL_LOAN, loan, format);
        loan.setAccountNumber(generated);
    }

    private WorkingCapitalBreach findBreachById(final Long breachId) {
        return breachRepository.findById(breachId)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.wclp.breach.not.found",
                        "Working Capital Breach with id " + breachId + " was not found.", breachId));
    }

    private WorkingCapitalNearBreach findNearBreachById(final Long nearBreachId) {
        return nearBreachRepository.findById(nearBreachId)
                .orElseThrow(() -> new GeneralPlatformDomainRuleException("error.msg.wclp.nearbreach.not.found",
                        "Working Capital Near Breach with id " + nearBreachId + " was not found.", nearBreachId));
    }
}
