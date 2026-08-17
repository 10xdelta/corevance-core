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

package com.corevance.infrastructure.core.config.jpa;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import java.util.List;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.persistence.ExtendedJpaTransactionManager;
import com.corevance.infrastructure.core.persistence.TransactionLifecycleCallback;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class JpaTransactionConfig {

    @Bean(name = { "transactionManager", "jpaTransactionManager" })
    @Primary
    public PlatformTransactionManager jpaTransactionManager(CorevanceProperties corevanceProperties,
            ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers, List<TransactionLifecycleCallback> callbacks) {
        boolean readOnly = corevanceProperties.getMode().isReadOnlyMode();
        ExtendedJpaTransactionManager transactionManager = new ExtendedJpaTransactionManager(readOnly);
        transactionManager.setLifecycleCallbacks(callbacks);
        transactionManager.setValidateExistingTransaction(true);
        transactionManagerCustomizers.ifAvailable(customizers -> customizers.customize(transactionManager));
        return transactionManager;
    }

    @Bean(name = { "transactionTemplate", "txTemplate", "jpaTransactionTemplate" })
    @Primary
    public TransactionTemplate jpaTransactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean("requiresNewTransactionTemplate")
    public TransactionTemplate requiresNewTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
        return transactionTemplate;
    }
}
