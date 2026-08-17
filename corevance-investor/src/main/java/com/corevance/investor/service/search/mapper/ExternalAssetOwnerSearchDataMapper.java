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
package com.corevance.investor.service.search.mapper;

import com.corevance.infrastructure.core.config.MapstructMapperConfig;
import com.corevance.investor.data.ExternalTransferData;
import com.corevance.investor.data.ExternalTransferDataDetails;
import com.corevance.investor.data.ExternalTransferLoanData;
import com.corevance.investor.data.ExternalTransferOwnerData;
import com.corevance.investor.data.ExternalTransferStatus;
import com.corevance.investor.data.ExternalTransferSubStatus;
import com.corevance.investor.domain.search.SearchedExternalAssetOwner;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapstructMapperConfig.class)
public interface ExternalAssetOwnerSearchDataMapper {

    @Mapping(target = "owner", source = "source", qualifiedByName = "toOwner")
    @Mapping(target = "loan", source = "source", qualifiedByName = "toLoanExternalId")
    @Mapping(target = "transferExternalId", source = "source", qualifiedByName = "toTransferExternalId")
    @Mapping(target = "status", source = "source", qualifiedByName = "toStatus")
    @Mapping(target = "subStatus", source = "source", qualifiedByName = "toSubStatus")
    @Mapping(target = "details", source = "source", qualifiedByName = "toDetails")
    @Mapping(target = "previousOwner", ignore = true)
    ExternalTransferData map(SearchedExternalAssetOwner source);

    @Named("toTransferExternalId")
    default String toTransferExternalId(SearchedExternalAssetOwner source) {
        return source.getTransferExternalId().getValue();
    }

    @Named("toLoanExternalId")
    default ExternalTransferLoanData toLoanExternalId(SearchedExternalAssetOwner source) {
        return new ExternalTransferLoanData(source.getLoanId(), source.getExternalLoanId().getValue());
    }

    @Named("toOwner")
    default ExternalTransferOwnerData toOwner(SearchedExternalAssetOwner source) {
        return new ExternalTransferOwnerData(source.getOwner().getValue());
    }

    @Named("toStatus")
    default ExternalTransferStatus toStatus(SearchedExternalAssetOwner source) {
        return source.getStatus();
    }

    @Named("toSubStatus")
    default ExternalTransferSubStatus toSubStatus(SearchedExternalAssetOwner source) {
        return source.getSubStatus();
    }

    @Named("toDetails")
    default ExternalTransferDataDetails toDetails(SearchedExternalAssetOwner source) {
        if (source.getDetailsId() == null) {
            return null;
        }
        return new ExternalTransferDataDetails(source.getDetailsId(), source.getTotalOutstanding(), source.getPrincipalOutstanding(),
                source.getInterestOutstanding(), source.getFeeOutstanding(), source.getPenaltyOutstanding(), source.getTotalOverpaid());
    }
}
