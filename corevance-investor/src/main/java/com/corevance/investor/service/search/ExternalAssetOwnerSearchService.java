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
package com.corevance.investor.service.search;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.core.service.PagedRequest;
import com.corevance.investor.data.ExternalTransferData;
import com.corevance.investor.domain.search.SearchingExternalAssetOwnerRepository;
import com.corevance.investor.service.search.domain.ExternalAssetOwnerSearchRequest;
import com.corevance.investor.service.search.mapper.ExternalAssetOwnerSearchDataMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExternalAssetOwnerSearchService {

    private final SearchingExternalAssetOwnerRepository externalAssetOwnerRepository;
    private final ExternalAssetOwnerSearchDataMapper externalAssetOwnerSearchDataMapper;

    public Page<ExternalTransferData> searchInvestorData(PagedRequest<ExternalAssetOwnerSearchRequest> searchRequest) {
        validateTextSearchRequest(searchRequest);
        return executeSearch(searchRequest);
    }

    private void validateTextSearchRequest(PagedRequest<ExternalAssetOwnerSearchRequest> searchRequest) {
        Objects.requireNonNull(searchRequest, "searchRequest must not be null");
    }

    private Page<ExternalTransferData> executeSearch(PagedRequest<ExternalAssetOwnerSearchRequest> searchRequest) {
        return externalAssetOwnerRepository.searchInvestorData(searchRequest).map(externalAssetOwnerSearchDataMapper::map);
    }

}
