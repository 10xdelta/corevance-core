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
package com.corevance.portfolio.loanproduct.mapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.corevance.infrastructure.core.config.MapstructMapperConfig;
import com.corevance.portfolio.loanaccount.domain.LoanPaymentAllocationRule;
import com.corevance.portfolio.loanproduct.data.AdvancedPaymentData;
import com.corevance.portfolio.loanproduct.domain.LoanProductPaymentAllocationRule;
import com.corevance.portfolio.loanproduct.domain.PaymentAllocationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class)
public interface AdvancedPaymentDataMapper {

    List<AdvancedPaymentData> mapLoanProductPaymentAllocationRule(List<LoanProductPaymentAllocationRule> paymentAllocationRule);

    List<AdvancedPaymentData> mapLoanPaymentAllocationRule(List<LoanPaymentAllocationRule> paymentAllocationRule);

    @Mapping(target = "paymentAllocationOrder", source = "allocationTypes")
    AdvancedPaymentData mapLoanProductPaymentAllocationRule(LoanProductPaymentAllocationRule paymentAllocationRule);

    @Mapping(target = "paymentAllocationOrder", source = "allocationTypes")
    AdvancedPaymentData mapLoanPaymentAllocationRule(LoanPaymentAllocationRule paymentAllocationRule);

    default List<AdvancedPaymentData.PaymentAllocationOrder> mapAllocationTypes(List<PaymentAllocationType> allocationTypes) {
        AtomicInteger counter = new AtomicInteger(1);
        return allocationTypes.stream().map(a -> new AdvancedPaymentData.PaymentAllocationOrder(a.name(), counter.getAndIncrement()))
                .toList();
    }
}
