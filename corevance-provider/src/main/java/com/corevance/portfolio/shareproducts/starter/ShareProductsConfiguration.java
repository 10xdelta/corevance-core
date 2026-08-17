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
package com.corevance.portfolio.shareproducts.starter;

import com.corevance.accounting.common.AccountingDropdownReadPlatformService;
import com.corevance.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import com.corevance.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.PaginationHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.utils.ColumnValidator;
import com.corevance.organisation.monetary.service.CurrencyReadPlatformService;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.products.service.ShareProductReadPlatformService;
import com.corevance.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import com.corevance.portfolio.shareproducts.domain.ShareProductDividentPayOutDetailsRepositoryWrapper;
import com.corevance.portfolio.shareproducts.domain.ShareProductRepositoryWrapper;
import com.corevance.portfolio.shareproducts.serialization.ShareProductDataSerializer;
import com.corevance.portfolio.shareproducts.service.ShareProductCommandsServiceImpl;
import com.corevance.portfolio.shareproducts.service.ShareProductDividendAssembler;
import com.corevance.portfolio.shareproducts.service.ShareProductDividendReadPlatformService;
import com.corevance.portfolio.shareproducts.service.ShareProductDividendReadPlatformServiceImpl;
import com.corevance.portfolio.shareproducts.service.ShareProductDropdownReadPlatformService;
import com.corevance.portfolio.shareproducts.service.ShareProductDropdownReadPlatformServiceImpl;
import com.corevance.portfolio.shareproducts.service.ShareProductReadPlatformServiceImpl;
import com.corevance.portfolio.shareproducts.service.ShareProductWritePlatformService;
import com.corevance.portfolio.shareproducts.service.ShareProductWritePlatformServiceJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ShareProductsConfiguration {

    @Bean(value = "SHAREPRODUCT_COMMANDSERVICE")
    @ConditionalOnMissingBean(ShareProductCommandsServiceImpl.class)
    public ShareProductCommandsServiceImpl shareProductCommandsService(FromJsonHelper fromApiJsonHelper) {
        return new ShareProductCommandsServiceImpl(fromApiJsonHelper);

    }

    @Bean
    @ConditionalOnMissingBean(ShareProductDividendAssembler.class)
    public ShareProductDividendAssembler shareProductDividendAssembler(ShareProductReadPlatformService shareProductReadPlatformService,
            ShareAccountReadPlatformService shareAccountReadPlatformService) {
        return new ShareProductDividendAssembler(shareProductReadPlatformService, shareAccountReadPlatformService);

    }

    @Bean
    @ConditionalOnMissingBean(ShareProductDividendReadPlatformService.class)
    public ShareProductDividendReadPlatformService shareProductDividendReadPlatformService(JdbcTemplate jdbcTemplate,
            ColumnValidator columnValidator, PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new ShareProductDividendReadPlatformServiceImpl(jdbcTemplate, columnValidator, paginationHelper, sqlGenerator);

    }

    @Bean
    @ConditionalOnMissingBean(ShareProductDropdownReadPlatformService.class)
    public ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService() {
        return new ShareProductDropdownReadPlatformServiceImpl();
    }

    @Bean(value = "shareReadPlatformService")
    @ConditionalOnMissingBean(ShareProductReadPlatformService.class)
    public ShareProductReadPlatformService shareProductReadPlatformService(JdbcTemplate jdbcTemplate,
            CurrencyReadPlatformService currencyReadPlatformService, ChargeReadPlatformService chargeReadPlatformService,
            ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService,
            AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
            ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService,
            PaginationHelper shareProductDataPaginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new ShareProductReadPlatformServiceImpl(jdbcTemplate, currencyReadPlatformService, chargeReadPlatformService,
                shareProductDropdownReadPlatformService, accountingDropdownReadPlatformService, accountMappingReadPlatformService,
                shareProductDataPaginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(ShareProductWritePlatformService.class)
    public ShareProductWritePlatformService shareProductWritePlatformService(ShareProductRepositoryWrapper repository,
            ShareProductDataSerializer serializer, FromJsonHelper fromApiJsonHelper,
            ShareProductDividentPayOutDetailsRepositoryWrapper shareProductDividentPayOutDetailsRepository,
            ShareProductDividendAssembler shareProductDividendAssembler,
            ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService,
            BusinessEventNotifierService businessEventNotifierService) {
        return new ShareProductWritePlatformServiceJpaRepositoryImpl(repository, serializer, fromApiJsonHelper,
                shareProductDividentPayOutDetailsRepository, shareProductDividendAssembler, accountMappingWritePlatformService,
                businessEventNotifierService);
    }

}
