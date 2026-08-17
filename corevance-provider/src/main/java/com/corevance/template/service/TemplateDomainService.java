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
package com.corevance.template.service;

import java.util.List;
import com.corevance.template.data.TemplateCreateRequest;
import com.corevance.template.data.TemplateCreateResponse;
import com.corevance.template.data.TemplateData;
import com.corevance.template.data.TemplateDeleteRequest;
import com.corevance.template.data.TemplateDeleteResponse;
import com.corevance.template.data.TemplateUpdateRequest;
import com.corevance.template.data.TemplateUpdateResponse;
import com.corevance.template.domain.TemplateEntity;
import com.corevance.template.domain.TemplateType;

public interface TemplateDomainService {

    List<TemplateData> getAll();

    List<TemplateData> getAllByEntityAndType(TemplateEntity entity, TemplateType type);

    List<TemplateData> getTemplate(String mapperkey, String mappervalue);

    TemplateData findOneById(Long id);

    TemplateCreateResponse createTemplate(TemplateCreateRequest request);

    TemplateUpdateResponse updateTemplate(TemplateUpdateRequest request);

    TemplateDeleteResponse removeTemplate(TemplateDeleteRequest request);
}
