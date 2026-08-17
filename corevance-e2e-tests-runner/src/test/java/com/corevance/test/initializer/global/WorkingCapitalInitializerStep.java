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
package com.corevance.test.initializer.global;

import static com.corevance.client.feign.util.FeignCalls.ok;
import static com.corevance.test.factory.WorkingCapitalRequestFactory.DUE_FEE;
import static com.corevance.test.factory.WorkingCapitalRequestFactory.DUE_PENALTY;
import static com.corevance.test.factory.WorkingCapitalRequestFactory.DUE_PRINCIPAL;
import static com.corevance.test.factory.WorkingCapitalRequestFactory.IN_ADVANCE_FEE;
import static com.corevance.test.factory.WorkingCapitalRequestFactory.IN_ADVANCE_PENALTY;
import static com.corevance.test.factory.WorkingCapitalRequestFactory.IN_ADVANCE_PRINCIPAL;
import static com.corevance.test.factory.WorkingCapitalRequestFactory.createPaymentAllocation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.models.GetWorkingCapitalLoanProductsResponse;
import com.corevance.client.models.PostAllowAttributeOverrides;
import com.corevance.client.models.PostPaymentAllocation;
import com.corevance.client.models.PostWorkingCapitalLoanProductsRequest;
import com.corevance.client.models.PostWorkingCapitalLoanProductsResponse;
import com.corevance.test.data.accounttype.AccountTypeResolver;
import com.corevance.test.data.accounttype.DefaultAccountType;
import com.corevance.test.data.codevalue.CodeValueResolver;
import com.corevance.test.data.codevalue.DefaultCodeValue;
import com.corevance.test.data.paymenttype.DefaultPaymentType;
import com.corevance.test.data.paymenttype.PaymentTypeResolver;
import com.corevance.test.data.workingcapitalproduct.DefaultWorkingCapitalLoanProduct;
import com.corevance.test.factory.WorkingCapitalRequestFactory;
import com.corevance.test.helper.CodeHelper;
import com.corevance.test.helper.ParallelExecutionHelper;
import com.corevance.test.support.TestContext;
import com.corevance.test.support.TestContextKey;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class WorkingCapitalInitializerStep implements CorevanceGlobalInitializerStep {

    private final CorevanceFeignClient corevanceClient;
    private final WorkingCapitalRequestFactory workingCapitalRequestFactory;
    private final PaymentTypeResolver paymentTypeResolver;
    private final AccountTypeResolver accountTypeResolver;
    private final CodeValueResolver codeValueResolver;
    private final CodeHelper codeHelper;

    @Override
    public void initialize() throws Exception {
        PostAllowAttributeOverrides allowAttributeOverridesDisabled = new PostAllowAttributeOverrides()
                .delinquencyBucketClassification(false).discountDefault(false).periodPaymentFrequencyType(false)
                .periodPaymentFrequency(false).breach(false);

        PostAllowAttributeOverrides allowAttributeOverrides = new PostAllowAttributeOverrides().delinquencyBucketClassification(true)
                .breach(true).discountDefault(true).periodPaymentFrequencyType(true).periodPaymentFrequency(true);

        // Retrieve code IDs for charge-off and write-off reasons
        final Long chargeOffReasonCodeId = codeHelper.retrieveCodeByName("ChargeOffReasons").getId();
        final Long writeOffReasonCodeId = codeHelper.retrieveCodeByName("WriteOffReasons").getId();

        List<Runnable> items = List.of(
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP.getName()))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DISCOUNT,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_DISCOUNT.getName()).discount(new BigDecimal(50)))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DISALLOW_OVERRIDES,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_DISALLOW_ATTRIBUTES_OVERRIDE.getName()))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DISCOUNT_DISALLOW_OVERRIDES,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_DISCOUNT_DISALLOW_ATTRIBUTES_OVERRIDE.getName())
                                .discount(new BigDecimal(50)).allowAttributeOverrides(allowAttributeOverridesDisabled))),
                () -> {
                    PostWorkingCapitalLoanProductsRequest req = workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequest()
                            .name(DefaultWorkingCapitalLoanProduct.WCLP_FOR_UPDATE.getName());
                    PostWorkingCapitalLoanProductsResponse response = createWorkingCapitalLoanProductIdempotent(req);
                    TestContext.GLOBAL.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_REQUEST_FOR_UPDATE_WCLP, req);
                    TestContext.GLOBAL.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_FOR_UPDATE_WCLP, response);
                },
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DELINQUENCY_RESCHEDULE,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_DELINQUENCY_RESCHEDULE.getName()))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_BREACH,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductBreachRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_BREACH.getName()))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_BREACH_NEAR_BREACH,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductBreachNearBreachRequest()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_BREACH_NEAR_BREACH.getName()))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_BREACH_DISALLOW_OVERRIDES,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductBreachRequest().allowAttributeOverrides(allowAttributeOverridesDisabled)
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE.getName()))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_BREACH_NEAR_BREACH_DISALLOW_OVERRIDES,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductBreachNearBreachRequest()
                                        .allowAttributeOverrides(allowAttributeOverridesDisabled)
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_BREACH_NEAR_BREACH_DISALLOW_ATTRIBUTES_OVERRIDE
                                                .getName()))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_ADVANCED_ACCOUNTING,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_ADVANCED_ACCOUNTING.getName())
                                .allowAttributeOverrides(allowAttributeOverrides)
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentChannelToFundSourceMappings(
                                        List.of(new com.corevance.client.models.WorkingCapitalLoanPaymentChannelToFundSourceMappings()
                                                .paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER))
                                                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES))))
                                .chargeOffReasonToExpenseAccountMappings(List.of(
                                        new com.corevance.client.models.WorkingCapitalPostChargeOffReasonToExpenseAccountMappings()
                                                .chargeOffReasonCodeValueId(
                                                        codeValueResolver.resolve(chargeOffReasonCodeId, DefaultCodeValue.valueOf("FRAUD")))
                                                .expenseAccountId(
                                                        accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT_FRAUD))))
                                .writeOffReasonsToExpenseMappings(List
                                        .of(new com.corevance.client.models.WorkingCapitalPostWriteOffReasonToExpenseAccountMappings()
                                                .writeOffReasonCodeValueId(codeValueResolver.resolve(writeOffReasonCodeId,
                                                        DefaultCodeValue.valueOf("BAD_DEBT")))
                                                .expenseAccountId(accountTypeResolver.resolve(DefaultAccountType.CREDIT_LOSS_BAD_DEBT)))))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_ACC_DEF_REV_AM,
                        createWorkingCapitalLoanProductIdempotent(
                                workingCapitalRequestFactory.defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                        .name(DefaultWorkingCapitalLoanProduct.WCLP_ACC_DEF_REV_AM.getName())
                                        .allowAttributeOverrides(allowAttributeOverrides).overpaymentLiabilityAccountId(
                                                accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY)))),
                () -> TestContext.INSTANCE.set(TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_PERIOD_PAYMENT_RATE,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductAllowAttributesOverrideRequest().minPeriodPaymentRate(new BigDecimal(1))
                                .maxPeriodPaymentRate(new BigDecimal(95)).periodPaymentRate(new BigDecimal(10))
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_PERIOD_PAYMENT_RATE.getName()))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DUE_FEE_PENALTY_PRINCIPAL,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_DUE_FEE_PENALTY_PRINCIPAL.getName())
                                .allowAttributeOverrides(allowAttributeOverrides)
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentAllocation(List.of(
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(),
                                                List.of(DUE_FEE, DUE_PENALTY, DUE_PRINCIPAL, IN_ADVANCE_FEE, IN_ADVANCE_PENALTY,
                                                        IN_ADVANCE_PRINCIPAL)),
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.REPAYMENT.getValue(),
                                                List.of(DUE_FEE, DUE_PENALTY, DUE_PRINCIPAL, IN_ADVANCE_FEE, IN_ADVANCE_PENALTY,
                                                        IN_ADVANCE_PRINCIPAL))))
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentChannelToFundSourceMappings(
                                        List.of(new com.corevance.client.models.WorkingCapitalLoanPaymentChannelToFundSourceMappings()
                                                .paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER))
                                                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES)))))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_IN_ADVANCE_PENALTY_FEE_PRINCIPAL,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_IN_ADVANCE_PENALTY_FEE_PRINCIPAL.getName())
                                .allowAttributeOverrides(allowAttributeOverrides)
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentAllocation(List.of(
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(),
                                                List.of(IN_ADVANCE_PENALTY, IN_ADVANCE_FEE, IN_ADVANCE_PRINCIPAL, DUE_PENALTY, DUE_FEE,
                                                        DUE_PRINCIPAL)),
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.REPAYMENT.getValue(),
                                                List.of(IN_ADVANCE_PENALTY, IN_ADVANCE_FEE, IN_ADVANCE_PRINCIPAL, DUE_PENALTY, DUE_FEE,
                                                        DUE_PRINCIPAL))))
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentChannelToFundSourceMappings(
                                        List.of(new com.corevance.client.models.WorkingCapitalLoanPaymentChannelToFundSourceMappings()
                                                .paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER))
                                                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES)))))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DUE_FEE_PRINCIPAL_PENALTY,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_DUE_FEE_PRINCIPAL_PENALTY.getName())
                                .allowAttributeOverrides(allowAttributeOverrides)
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentAllocation(List.of(
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(),
                                                List.of(DUE_FEE, DUE_PRINCIPAL, DUE_PENALTY, IN_ADVANCE_FEE, IN_ADVANCE_PRINCIPAL,
                                                        IN_ADVANCE_PENALTY)),
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.REPAYMENT.getValue(),
                                                List.of(DUE_FEE, DUE_PRINCIPAL, DUE_PENALTY, IN_ADVANCE_FEE, IN_ADVANCE_PRINCIPAL,
                                                        IN_ADVANCE_PENALTY)))))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_DUE_PRINCIPAL_FEE_PENALTY,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_DUE_PRINCIPAL_FEE_PENALTY.getName())
                                .allowAttributeOverrides(allowAttributeOverrides)
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentAllocation(List.of(
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(),
                                                List.of(DUE_PRINCIPAL, DUE_FEE, DUE_PENALTY, IN_ADVANCE_PRINCIPAL, IN_ADVANCE_FEE,
                                                        IN_ADVANCE_PENALTY)),
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.REPAYMENT.getValue(),
                                                List.of(DUE_PRINCIPAL, DUE_FEE, DUE_PENALTY, IN_ADVANCE_PRINCIPAL, IN_ADVANCE_FEE,
                                                        IN_ADVANCE_PENALTY))))
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentChannelToFundSourceMappings(
                                        List.of(new com.corevance.client.models.WorkingCapitalLoanPaymentChannelToFundSourceMappings()
                                                .paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER))
                                                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES)))))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_GOODWILL_CREDIT_ALLOCATION,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_GOODWILL_CREDIT_ALLOCATION.getName())
                                .allowAttributeOverrides(allowAttributeOverrides)
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                .paymentAllocation(List.of(
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(),
                                                List.of(DUE_FEE, DUE_PENALTY, DUE_PRINCIPAL, IN_ADVANCE_FEE, IN_ADVANCE_PENALTY,
                                                        IN_ADVANCE_PRINCIPAL)),
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.REPAYMENT.getValue(),
                                                List.of(DUE_FEE, DUE_PENALTY, DUE_PRINCIPAL, IN_ADVANCE_FEE, IN_ADVANCE_PENALTY,
                                                        IN_ADVANCE_PRINCIPAL)),
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.GOODWILL_CREDIT.getValue(),
                                                List.of(DUE_PRINCIPAL, DUE_FEE, DUE_PENALTY, IN_ADVANCE_PRINCIPAL, IN_ADVANCE_FEE,
                                                        IN_ADVANCE_PENALTY))))
                                .paymentChannelToFundSourceMappings(
                                        List.of(new com.corevance.client.models.WorkingCapitalLoanPaymentChannelToFundSourceMappings()
                                                .paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER))
                                                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES)))))),
                () -> TestContext.INSTANCE.set(
                        TestContextKey.DEFAULT_WORKING_CAPITAL_LOAN_PRODUCT_CREATE_RESPONSE_WCLP_REPAYMENT_DIFF_DEFAULT,
                        createWorkingCapitalLoanProductIdempotent(workingCapitalRequestFactory
                                .defaultWorkingCapitalLoanProductRequestWithAccrualAccounting()
                                .name(DefaultWorkingCapitalLoanProduct.WCLP_REPAYMENT_DIFF_DEFAULT.getName())
                                .allowAttributeOverrides(allowAttributeOverrides)
                                .overpaymentLiabilityAccountId(accountTypeResolver.resolve(DefaultAccountType.OTHER_CREDIT_LIABILITY))
                                // DEFAULT differs from REPAYMENT on purpose and no GOODWILL_CREDIT rule is configured:
                                // goodwill credits must fall back to DEFAULT (principal-first), not to REPAYMENT
                                // (fee-first).
                                .paymentAllocation(List.of(
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.DEFAULT.getValue(),
                                                List.of(DUE_PRINCIPAL, DUE_FEE, DUE_PENALTY, IN_ADVANCE_PRINCIPAL, IN_ADVANCE_FEE,
                                                        IN_ADVANCE_PENALTY)),
                                        createPaymentAllocation(PostPaymentAllocation.TransactionTypeEnum.REPAYMENT.getValue(),
                                                List.of(DUE_FEE, DUE_PENALTY, DUE_PRINCIPAL, IN_ADVANCE_FEE, IN_ADVANCE_PENALTY,
                                                        IN_ADVANCE_PRINCIPAL))))
                                .paymentChannelToFundSourceMappings(
                                        List.of(new com.corevance.client.models.WorkingCapitalLoanPaymentChannelToFundSourceMappings()
                                                .paymentTypeId(paymentTypeResolver.resolve(DefaultPaymentType.MONEY_TRANSFER))
                                                .fundSourceAccountId(accountTypeResolver.resolve(DefaultAccountType.FUND_RECEIVABLES)))))));
        ParallelExecutionHelper.runInParallel(items);
    }

    private PostWorkingCapitalLoanProductsResponse createWorkingCapitalLoanProductIdempotent(
            PostWorkingCapitalLoanProductsRequest workingCapitalProductRequest) {
        String workingCapitalProductName = workingCapitalProductRequest.getName();
        log.debug("Attempting to create working capital product: {}", workingCapitalProductName);
        try {
            List<GetWorkingCapitalLoanProductsResponse> existingWorkingCapitalProducts = corevanceClient.workingCapitalLoanProducts()
                    .retrieveAllWorkingCapitalLoanProducts(Map.of());
            GetWorkingCapitalLoanProductsResponse existingWorkingCapitalProduct = existingWorkingCapitalProducts.stream()
                    .filter(p -> workingCapitalProductName.equals(p.getName())).findFirst().orElse(null);

            if (existingWorkingCapitalProduct != null) {
                log.debug("Working capital product '{}' already exists with ID: {}", workingCapitalProductName,
                        existingWorkingCapitalProduct.getId());
                PostWorkingCapitalLoanProductsResponse response = new PostWorkingCapitalLoanProductsResponse();
                response.setResourceId(existingWorkingCapitalProduct.getId());
                return response;
            }
        } catch (Exception e) {
            log.warn("Error checking if working capital product '{}' exists", workingCapitalProductName, e);
        }

        log.debug("Creating new working capital product: {}", workingCapitalProductName);
        try {
            PostWorkingCapitalLoanProductsResponse response = ok(() -> corevanceClient.workingCapitalLoanProducts()
                    .createWorkingCapitalLoanProduct(workingCapitalProductRequest, Map.of()));
            log.debug("Successfully created working capital product '{}' with ID: {}", workingCapitalProductName, response.getResourceId());
            return response;
        } catch (Exception e) {
            log.error("FAILED to create working capital product '{}'", workingCapitalProductName, e);
            throw e;
        }
    }

}
