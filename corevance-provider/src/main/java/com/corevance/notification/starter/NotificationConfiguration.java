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
package com.corevance.notification.starter;

import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.event.business.service.BusinessEventNotifierService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.notification.domain.NotificationMapperRepository;
import com.corevance.notification.eventandlistener.NotificationEventPublisher;
import com.corevance.notification.service.NotificationDomainService;
import com.corevance.notification.service.NotificationDomainServiceImpl;
import com.corevance.notification.service.NotificationGeneratorReadRepositoryWrapper;
import com.corevance.notification.service.NotificationGeneratorWritePlatformService;
import com.corevance.notification.service.NotificationMapperWritePlatformService;
import com.corevance.notification.service.NotificationReadPlatformService;
import com.corevance.notification.service.NotificationReadPlatformServiceImpl;
import com.corevance.notification.service.NotificationWritePlatformService;
import com.corevance.notification.service.NotificationWritePlatformServiceImpl;
import com.corevance.notification.service.UserNotificationService;
import com.corevance.notification.service.UserNotificationServiceImpl;
import com.corevance.useradministration.domain.AppUserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfiguration {

    @Bean
    @ConditionalOnMissingBean(NotificationDomainService.class)
    public NotificationDomainService notificationDomainService(BusinessEventNotifierService businessEventNotifierService,
            PlatformSecurityContext context, UserNotificationService userNotificationService) {
        return new NotificationDomainServiceImpl(businessEventNotifierService, context, userNotificationService);
    }

    @Bean
    @ConditionalOnMissingBean(NotificationReadPlatformService.class)
    public NotificationReadPlatformService notificationReadPlatformService(PlatformSecurityContext context,
            NotificationMapperRepository notificationMapperRepository) {
        return new NotificationReadPlatformServiceImpl(context, notificationMapperRepository);
    }

    @Bean
    @ConditionalOnMissingBean(NotificationWritePlatformService.class)
    public NotificationWritePlatformService notificationWritePlatformService(
            NotificationGeneratorWritePlatformService notificationGeneratorWritePlatformService,
            NotificationGeneratorReadRepositoryWrapper notificationGeneratorReadRepositoryWrapper, AppUserRepository appUserRepository,
            NotificationMapperWritePlatformService notificationMapperWritePlatformService) {
        return new NotificationWritePlatformServiceImpl(notificationGeneratorWritePlatformService,
                notificationGeneratorReadRepositoryWrapper, appUserRepository, notificationMapperWritePlatformService);
    }

    @Bean
    @ConditionalOnMissingBean(UserNotificationService.class)
    public UserNotificationService userNotificationService(NotificationEventPublisher notificationEventPublisher,
            AppUserRepository appUserRepository, CorevanceProperties corevanceProperties,
            NotificationReadPlatformService notificationReadPlatformService,
            NotificationWritePlatformService notificationWritePlatformService) {
        return new UserNotificationServiceImpl(notificationEventPublisher, appUserRepository, corevanceProperties,
                notificationReadPlatformService, notificationWritePlatformService);
    }
}
