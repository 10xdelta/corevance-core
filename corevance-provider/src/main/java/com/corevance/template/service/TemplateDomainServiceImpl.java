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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.command.jdbc.store.mapping.CommandMapper;
import com.corevance.template.data.TemplateCreateRequest;
import com.corevance.template.data.TemplateCreateResponse;
import com.corevance.template.data.TemplateData;
import com.corevance.template.data.TemplateDeleteRequest;
import com.corevance.template.data.TemplateDeleteResponse;
import com.corevance.template.data.TemplateUpdateRequest;
import com.corevance.template.data.TemplateUpdateResponse;
import com.corevance.template.domain.Template;
import com.corevance.template.domain.TemplateEntity;
import com.corevance.template.domain.TemplateRepository;
import com.corevance.template.domain.TemplateType;
import com.corevance.template.exception.TemplateNotFoundException;
import com.corevance.template.exception.TemplateTypeInvalidException;
import com.corevance.template.mapper.TemplateMapper;
import com.corevance.template.mapper.TemplateMapperDataMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = TemplateDomainService.class, ignored = TemplateDomainServiceImpl.class)
public class TemplateDomainServiceImpl implements TemplateDomainService {

    private final CommandMapper commandMapper;

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;
    private final TemplateMapperDataMapper templateMapperDataMapper;

    @Override
    public List<TemplateData> getAll() {
        return templateMapper.map(templateRepository.findAll());
    }

    @Override
    public TemplateData findOneById(final Long id) {
        return templateRepository.findById(id).map(templateMapper::map).orElseThrow(() -> new TemplateNotFoundException(id));
    }

    @Override
    public List<TemplateData> getTemplate(String key, String templateName) {
        return templateMapper.map(templateRepository.findByTemplateMapper(key, templateName));
    }

    @Transactional
    @Override
    public TemplateCreateResponse createTemplate(TemplateCreateRequest request) {
        // FIXME: no validation here of the data in the command object, is
        // name, text populated etc
        // FIXME: handle cases where data integrity constraints are fired from
        // database when saving.

        var template = new Template().setName(request.getName()).setType(TemplateType.values()[request.getType()])
                .setEntity(TemplateEntity.values()[request.getEntity()]).setText(request.getText());

        templateRepository.saveAndFlush(template);

        return TemplateCreateResponse.builder().resourceId(template.getId()).build();
    }

    @Transactional
    @Override
    public TemplateUpdateResponse updateTemplate(TemplateUpdateRequest request) {
        // FIXME: no validation here of the data in the command object, is
        // name, text populated etc
        // FIXME: handle cases where data integrity constraints are fired from
        // database when saving.

        var template = templateRepository.findById(request.getId()).orElseThrow(() -> new TemplateNotFoundException(request.getId()));
        template.setName(request.getName());
        template.setText(request.getText());
        template.setEntity(TemplateEntity.values()[request.getEntity()]);

        switch (request.getType()) {
            case 0:
                template.setType(TemplateType.DOCUMENT);
            break;
            case 2:
                template.setType(TemplateType.SMS);
            break;
            default:
                throw new TemplateTypeInvalidException(request.getType());
        }

        template.setMappers(templateMapperDataMapper.map(request.getMappers()));

        this.templateRepository.saveAndFlush(template);

        return TemplateUpdateResponse.builder().resourceId(request.getId()).build();
    }

    @Transactional
    @Override
    public TemplateDeleteResponse removeTemplate(TemplateDeleteRequest request) {
        var template = templateRepository.findById(request.getId()).orElseThrow(() -> new TemplateNotFoundException(request.getId()));

        this.templateRepository.delete(template);

        return TemplateDeleteResponse.builder().resourceId(request.getId()).build();
    }

    @Override
    public List<TemplateData> getAllByEntityAndType(final TemplateEntity entity, final TemplateType type) {
        return templateMapper.map(templateRepository.findByEntityAndType(entity, type));
    }
}
