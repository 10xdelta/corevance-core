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
package com.corevance.portfolio.shareaccounts.start;

import com.corevance.accounting.journalentry.service.JournalEntryWritePlatformService;
import com.corevance.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.core.service.PaginationHelper;
import com.corevance.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.utils.ColumnValidator;
import com.corevance.portfolio.account.service.AccountNumberGenerator;
import com.corevance.portfolio.accounts.constants.AccountsApiConstants;
import com.corevance.portfolio.accounts.service.AccountsCommandsService;
import com.corevance.portfolio.charge.service.ChargeReadPlatformService;
import com.corevance.portfolio.client.service.ClientReadPlatformService;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountAssembler;
import com.corevance.portfolio.savings.service.SavingsAccountDomainService;
import com.corevance.portfolio.savings.service.SavingsAccountReadPlatformService;
import com.corevance.portfolio.shareaccounts.domain.ShareAccountDividendRepository;
import com.corevance.portfolio.shareaccounts.domain.ShareAccountRepositoryWrapper;
import com.corevance.portfolio.shareaccounts.serialization.ShareAccountDataSerializer;
import com.corevance.portfolio.shareaccounts.service.PurchasedSharesReadPlatformService;
import com.corevance.portfolio.shareaccounts.service.PurchasedSharesReadPlatformServiceImpl;
import com.corevance.portfolio.shareaccounts.service.ShareAccountChargeReadPlatformService;
import com.corevance.portfolio.shareaccounts.service.ShareAccountChargeReadPlatformServiceImpl;
import com.corevance.portfolio.shareaccounts.service.ShareAccountCommandsServiceImpl;
import com.corevance.portfolio.shareaccounts.service.ShareAccountDividendReadPlatformService;
import com.corevance.portfolio.shareaccounts.service.ShareAccountDividendReadPlatformServiceImpl;
import com.corevance.portfolio.shareaccounts.service.ShareAccountReadPlatformService;
import com.corevance.portfolio.shareaccounts.service.ShareAccountReadPlatformServiceImpl;
import com.corevance.portfolio.shareaccounts.service.ShareAccountSchedularService;
import com.corevance.portfolio.shareaccounts.service.ShareAccountSchedularServiceImpl;
import com.corevance.portfolio.shareaccounts.service.ShareAccountWritePlatformService;
import com.corevance.portfolio.shareaccounts.service.ShareAccountWritePlatformServiceJpaRepositoryImpl;
import com.corevance.portfolio.shareproducts.domain.ShareProductRepositoryWrapper;
import com.corevance.portfolio.shareproducts.service.ShareProductDropdownReadPlatformService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ShareAccountsConfiguration {

    @Bean
    @ConditionalOnMissingBean(PurchasedSharesReadPlatformService.class)
    public PurchasedSharesReadPlatformService purchasedSharesReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new PurchasedSharesReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(ShareAccountChargeReadPlatformService.class)
    public ShareAccountChargeReadPlatformService shareAccountChargeReadPlatformService(JdbcTemplate jdbcTemplate) {
        return new ShareAccountChargeReadPlatformServiceImpl(jdbcTemplate);
    }

    @Bean(value = "SHAREACCOUNT_COMMANDSERVICE")
    @ConditionalOnMissingBean(AccountsCommandsService.class)
    public AccountsCommandsService accountsCommandsService(FromJsonHelper fromApiJsonHelper,
            ShareAccountDataSerializer shareAccountDataSerializer) {
        return new ShareAccountCommandsServiceImpl(fromApiJsonHelper, shareAccountDataSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(ShareAccountDividendReadPlatformService.class)
    public ShareAccountDividendReadPlatformService shareAccountDividendReadPlatformService(JdbcTemplate jdbcTemplate,
            ColumnValidator columnValidator, PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new ShareAccountDividendReadPlatformServiceImpl(jdbcTemplate, columnValidator, paginationHelper, sqlGenerator);

    }

    @Bean(value = "share" + AccountsApiConstants.READPLATFORM_NAME)
    @ConditionalOnMissingBean(ShareAccountReadPlatformService.class)
    public ShareAccountReadPlatformService shareAccountReadPlatformService(ApplicationContext applicationContext,
            ChargeReadPlatformService chargeReadPlatformService,
            ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService,
            SavingsAccountReadPlatformService savingsAccountReadPlatformService, ClientReadPlatformService clientReadPlatformService,
            ShareAccountChargeReadPlatformService shareAccountChargeReadPlatformService,
            PurchasedSharesReadPlatformService purchasedSharesReadPlatformService, JdbcTemplate jdbcTemplate,
            PaginationHelper paginationHelper, DatabaseSpecificSQLGenerator sqlGenerator) {
        return new ShareAccountReadPlatformServiceImpl(applicationContext, chargeReadPlatformService,
                shareProductDropdownReadPlatformService, savingsAccountReadPlatformService, clientReadPlatformService,
                shareAccountChargeReadPlatformService, purchasedSharesReadPlatformService, jdbcTemplate, paginationHelper, sqlGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(ShareAccountSchedularService.class)
    public ShareAccountSchedularService shareAccountSchedularService(ShareAccountDividendRepository shareAccountDividendRepository,
            SavingsAccountDomainService savingsAccountDomainService, SavingsAccountAssembler savingsAccountAssembler) {
        return new ShareAccountSchedularServiceImpl(shareAccountDividendRepository, savingsAccountDomainService, savingsAccountAssembler);
    }

    @Bean
    @ConditionalOnMissingBean(ShareAccountWritePlatformService.class)
    public ShareAccountWritePlatformService shareAccountWritePlatformService(ShareAccountDataSerializer accountDataSerializer,
            ShareAccountRepositoryWrapper shareAccountRepository, ShareProductRepositoryWrapper shareProductRepository,
            AccountNumberGenerator accountNumberGenerator, AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            JournalEntryWritePlatformService journalEntryWritePlatformService, NoteRepository noteRepository,
            BusinessEventNotifierService businessEventNotifierService) {
        return new ShareAccountWritePlatformServiceJpaRepositoryImpl(accountDataSerializer, shareAccountRepository, shareProductRepository,
                accountNumberGenerator, accountNumberFormatRepository, journalEntryWritePlatformService, noteRepository,
                businessEventNotifierService);
    }
}
