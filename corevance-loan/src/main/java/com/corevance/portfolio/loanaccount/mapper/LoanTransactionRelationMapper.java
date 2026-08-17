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
package com.corevance.portfolio.loanaccount.mapper;

import java.util.List;
import com.corevance.infrastructure.core.config.MapstructMapperConfig;
import com.corevance.portfolio.loanaccount.data.LoanTransactionRelationData;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class)
public interface LoanTransactionRelationMapper {

    @Mapping(target = "fromLoanTransaction", source = "source.fromTransaction.id")
    @Mapping(target = "toLoanTransaction", source = "source.toTransaction.id")
    @Mapping(target = "toLoanCharge", source = "source.toCharge.id")
    @Mapping(target = "amount", source = "source.toTransaction.amount")
    @Mapping(target = "paymentType", source = "source.toTransaction.paymentDetail.paymentType.name")
    LoanTransactionRelationData map(LoanTransactionRelation source);

    List<LoanTransactionRelationData> map(List<LoanTransactionRelation> sources);

}
