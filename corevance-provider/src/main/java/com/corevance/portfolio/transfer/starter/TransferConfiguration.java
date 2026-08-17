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
package com.corevance.portfolio.transfer.starter;

import com.corevance.infrastructure.core.service.TransactionBoundApplicationEventPublisher;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.organisation.office.domain.OfficeRepositoryWrapper;
import com.corevance.organisation.staff.domain.StaffRepositoryWrapper;
import com.corevance.portfolio.calendar.domain.CalendarInstanceRepository;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.portfolio.client.domain.ClientTransferDetailsRepositoryWrapper;
import com.corevance.portfolio.group.domain.GroupRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.service.LoanOfficerService;
import com.corevance.portfolio.loanaccount.service.LoanWritePlatformService;
import com.corevance.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import com.corevance.portfolio.savings.service.SavingsAccountWritePlatformService;
import com.corevance.portfolio.transfer.data.TransfersDataValidator;
import com.corevance.portfolio.transfer.service.TransferWritePlatformService;
import com.corevance.portfolio.transfer.service.TransferWritePlatformServiceJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransferConfiguration {

    @Bean
    @ConditionalOnMissingBean(TransferWritePlatformService.class)
    public TransferWritePlatformService transferWritePlatformService(ClientRepositoryWrapper clientRepositoryWrapper,
            OfficeRepositoryWrapper officeRepository, CalendarInstanceRepository calendarInstanceRepository,
            LoanWritePlatformService loanWritePlatformService, GroupRepositoryWrapper groupRepository,
            LoanRepositoryWrapper loanRepositoryWrapper, TransfersDataValidator transfersDataValidator,
            StaffRepositoryWrapper staffRepositoryWrapper, SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper,
            SavingsAccountWritePlatformService savingsAccountWritePlatformService,
            ClientTransferDetailsRepositoryWrapper clientTransferDetailsRepositoryWrapper, PlatformSecurityContext context,
            LoanOfficerService loanOfficerService, TransactionBoundApplicationEventPublisher eventPublisher) {
        return new TransferWritePlatformServiceJpaRepositoryImpl(clientRepositoryWrapper, officeRepository, calendarInstanceRepository,
                groupRepository, loanWritePlatformService, savingsAccountWritePlatformService, loanRepositoryWrapper,
                savingsAccountRepositoryWrapper, transfersDataValidator, staffRepositoryWrapper, clientTransferDetailsRepositoryWrapper,
                context, loanOfficerService, eventPublisher);
    }
}
