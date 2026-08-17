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
package com.corevance.infrastructure.businessdate.mapper;

import java.util.List;
import com.corevance.infrastructure.businessdate.data.api.BusinessDateResponse;
import com.corevance.infrastructure.businessdate.data.api.BusinessDateUpdateRequest;
import com.corevance.infrastructure.businessdate.data.api.BusinessDateUpdateResponse;
import com.corevance.infrastructure.businessdate.data.service.BusinessDateDTO;
import com.corevance.infrastructure.businessdate.domain.BusinessDate;
import com.corevance.infrastructure.core.config.MapstructMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapstructMapperConfig.class)
public interface BusinessDateMapper {

    @Mapping(target = "description", source = "type.description")
    @Mapping(target = "changes", ignore = true)
    BusinessDateDTO mapEntity(BusinessDate source);

    List<BusinessDateDTO> mapEntity(List<BusinessDate> sources);

    @Mapping(target = "description", expression = "java(com.corevance.infrastructure.businessdate.domain.BusinessDateType.valueOf(source.getType()).getDescription())")
    @Mapping(target = "type", expression = "java(com.corevance.infrastructure.businessdate.domain.BusinessDateType.valueOf(source.getType()))")
    @Mapping(target = "date", expression = "java(com.corevance.infrastructure.core.service.DateUtils.toLocalDate(source.getLocale(), source.getDate(), source.getDateFormat()))")
    @Mapping(target = "changes", ignore = true)
    BusinessDateDTO mapUpdateRequest(BusinessDateUpdateRequest source);

    List<BusinessDateResponse> mapFetchResponse(List<BusinessDateDTO> sources);

    BusinessDateResponse mapFetchResponse(BusinessDateDTO source);

    BusinessDateUpdateResponse mapUpdateResponse(BusinessDateDTO source);
}
