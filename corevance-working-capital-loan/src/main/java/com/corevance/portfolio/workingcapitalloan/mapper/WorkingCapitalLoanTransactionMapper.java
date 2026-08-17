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
package com.corevance.portfolio.workingcapitalloan.mapper;

import com.corevance.infrastructure.codes.data.CodeValueData;
import com.corevance.infrastructure.codes.domain.CodeValue;
import com.corevance.infrastructure.core.config.MapstructMapperConfig;
import com.corevance.organisation.monetary.data.CurrencyData;
import com.corevance.portfolio.loanaccount.data.LoanTransactionEnumData;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionType;
import com.corevance.portfolio.loanproduct.service.LoanEnumerations;
import com.corevance.portfolio.paymentdetail.data.PaymentDetailData;
import com.corevance.portfolio.paymentdetail.domain.PaymentDetail;
import com.corevance.portfolio.paymenttype.data.PaymentTypeData;
import com.corevance.portfolio.paymenttype.domain.PaymentType;
import com.corevance.portfolio.workingcapitalloan.data.WorkingCapitalLoanTransactionData;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoan;
import com.corevance.portfolio.workingcapitalloan.domain.WorkingCapitalLoanTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface WorkingCapitalLoanTransactionMapper {

    @Mapping(target = "wcLoanId", source = "wcLoan.id")
    @Mapping(target = "type", source = "transactionType", qualifiedByName = "loanTransactionTypeToEnumData")
    @Mapping(target = "paymentDetailData", source = "paymentDetail", qualifiedByName = "paymentDetailToData")
    @Mapping(target = "classification", source = "classification", qualifiedByName = "codeValueToData")
    @Mapping(target = "transactionDate", source = "transactionDate")
    @Mapping(target = "principalPortion", source = "allocation.principalPortion")
    @Mapping(target = "feeChargesPortion", source = "allocation.feeChargesPortion")
    @Mapping(target = "penaltyChargesPortion", source = "allocation.penaltyChargesPortion")
    @Mapping(target = "currency", source = "wcLoan", qualifiedByName = "currencyData")
    WorkingCapitalLoanTransactionData toData(WorkingCapitalLoanTransaction transaction);

    @Named("loanTransactionTypeToEnumData")
    default LoanTransactionEnumData loanTransactionTypeToEnumData(final LoanTransactionType type) {
        return type == null ? null : LoanEnumerations.transactionType(type);
    }

    @Named("paymentDetailToData")
    default PaymentDetailData paymentDetailToData(final PaymentDetail paymentDetail) {
        if (paymentDetail == null) {
            return null;
        }
        return PaymentDetailData.builder().id(paymentDetail.getId()).paymentType(paymentTypeToData(paymentDetail.getPaymentType()))
                .accountNumber(paymentDetail.getAccountNumber()).checkNumber(paymentDetail.getCheckNumber())
                .routingCode(paymentDetail.getRoutingCode()).receiptNumber(paymentDetail.getReceiptNumber())
                .bankNumber(paymentDetail.getBankNumber()).build();
    }

    @Named("paymentTypeToData")
    default PaymentTypeData paymentTypeToData(final PaymentType paymentType) {
        if (paymentType == null) {
            return null;
        }
        return PaymentTypeData.builder().id(paymentType.getId()).name(paymentType.getName()).description(paymentType.getDescription())
                .isCashPayment(paymentType.getIsCashPayment()).position(paymentType.getPosition()).codeName(paymentType.getCodeName())
                .isSystemDefined(paymentType.getIsSystemDefined()).build();
    }

    @Named("codeValueToData")
    default CodeValueData codeValueToData(final CodeValue codeValue) {
        return codeValue == null ? null : CodeValueData.instance(codeValue.getId(), codeValue.getLabel());
    }

    @Named("currencyData")
    default CurrencyData currencyData(final WorkingCapitalLoan wcLoan) {
        return wcLoan.getLoanProduct().getCurrency().toData();
    }
}
