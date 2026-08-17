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
package com.corevance.infrastructure.dataqueries.starter;

import com.corevance.infrastructure.codes.service.CodeReadPlatformService;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.serialization.DatatableCommandFromApiJsonDeserializer;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.core.service.database.DatabaseTypeResolver;
import com.corevance.infrastructure.dataqueries.data.DataTableValidator;
import com.corevance.infrastructure.dataqueries.service.DatatableKeywordGenerator;
import com.corevance.infrastructure.dataqueries.service.DatatableReadService;
import com.corevance.infrastructure.dataqueries.service.DatatableReadServiceImpl;
import com.corevance.infrastructure.dataqueries.service.DatatableUtil;
import com.corevance.infrastructure.dataqueries.service.DatatableWriteService;
import com.corevance.infrastructure.dataqueries.service.DatatableWriteServiceImpl;
import com.corevance.infrastructure.dataqueries.service.GenericDataService;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.infrastructure.security.service.SqlValidator;
import com.corevance.portfolio.search.service.SearchUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class DataQueriesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DatatableReadService datatableReadService(final JdbcTemplate jdbcTemplate, final DatabaseSpecificSQLGenerator sqlGenerator,
            final PlatformSecurityContext context, final GenericDataService genericDataService, final DataTableValidator dataTableValidator,
            final SqlValidator sqlValidator, final SearchUtil searchUtil, final DatatableUtil datatableUtil) {
        return new DatatableReadServiceImpl(jdbcTemplate, sqlGenerator, context, genericDataService, dataTableValidator, sqlValidator,
                searchUtil, datatableUtil);
    }

    @Bean
    @ConditionalOnMissingBean
    public DatatableWriteService datatableWriteService(final JdbcTemplate jdbcTemplate, final DatabaseTypeResolver databaseTypeResolver,
            final DatabaseSpecificSQLGenerator sqlGenerator, final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
            final GenericDataService genericDataService, final DatatableCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final ConfigurationDomainService configurationDomainService, final CodeReadPlatformService codeReadPlatformService,
            final DataTableValidator dataTableValidator, final NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            final DatatableKeywordGenerator datatableKeywordGenerator, final SearchUtil searchUtil,
            final BusinessEventNotifierService businessEventNotifierService, final DatatableReadService datatableReadService,
            final DatatableUtil datatableUtil) {
        return new DatatableWriteServiceImpl(jdbcTemplate, databaseTypeResolver, sqlGenerator, context, fromJsonHelper, genericDataService,
                fromApiJsonDeserializer, configurationDomainService, codeReadPlatformService, dataTableValidator,
                namedParameterJdbcTemplate, datatableKeywordGenerator, searchUtil, businessEventNotifierService, datatableReadService,
                datatableUtil);
    }

}
