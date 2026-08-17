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

package com.corevance.infrastructure.core.config;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.corevance.infrastructure.security.domain.OidcFederationType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "corevance")
public class CorevanceProperties {

    private String nodeId;

    private String idempotencyKeyHeaderName;

    private Boolean insecureHttpClient;
    private long clientConnectTimeout;
    private long clientReadTimeout;
    private long clientWriteTimeout;

    private CorevanceTenantProperties tenant;

    private CorevanceModeProperties mode;

    private CorevanceCorrelationProperties correlation;

    private CorevanceIpTrackingProperties ipTracking;

    private CorevancePartitionedJob partitionedJob;

    private CorevanceRemoteJobMessageHandlerProperties remoteJobMessageHandler;

    private CorevanceEventsProperties events;

    private CorevanceTaskExecutor taskExecutor;

    private CorevanceContentProperties content;

    private CorevanceReportProperties report;

    private CorevanceJobProperties job;

    private CorevanceTemplateProperties template;
    private CorevanceJpaProperties jpa;

    private CorevanceDatabaseProperties database;
    private CorevanceQueryProperties query;
    private CorevanceApiProperties api;
    private CorevanceSecurityProperties security;

    private CorevanceNotificationProperties notification;

    private CorevanceLoanProperties loan;

    private CorevanceSamplingProperties sampling;

    private CorevanceModulesProperties module;

    private CorevanceSqlValidationProperties sqlValidation;
    private CorevanceInputValidationProperties inputValidation;

    private CorevanceCache cache;

    private RetryProperties retry;

    private CorevanceDefaultValues defaults;

    @Getter
    @Setter
    public static class CorevanceTenantProperties {

        private String host;
        private Integer port;
        private String username;
        private String password;
        private String parameters;
        private String timezone;
        private String identifier;
        private String name;
        private String description;
        private String masterPassword;
        private String encryption;

        private String readOnlyHost;
        private Integer readOnlyPort;
        private String readOnlyUsername;
        private String readOnlyPassword;
        private String readOnlyParameters;
        private String readOnlyName;

        private CorevanceConfigProperties config;
    }

    /**
     * Configuration properties to override configurations stored in the tenants database
     */
    @Getter
    @Setter
    public static class CorevanceConfigProperties {

        private int minPoolSize;
        private int maxPoolSize;
        private long leakDetectionThreshold;

        public boolean isMinPoolSizeSet() {
            return minPoolSize != -1;
        }

        public boolean isMaxPoolSizeSet() {
            return maxPoolSize != -1;
        }

        public boolean isLeakDetectionThresholdSet() {
            return leakDetectionThreshold > 0;
        }
    }

    @Getter
    @Setter
    public static class CorevanceModeProperties {

        private boolean readEnabled;
        private boolean writeEnabled;
        private boolean batchWorkerEnabled;
        private boolean batchManagerEnabled;

        public boolean isReadOnlyMode() {
            return readEnabled && !writeEnabled && !batchWorkerEnabled && !batchManagerEnabled;
        }
    }

    @Getter
    @Setter
    public static class CorevanceCorrelationProperties {

        private boolean enabled;
        private String headerName;
    }

    @Getter
    @Setter
    public static class CorevanceIpTrackingProperties {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class CorevancePartitionedJob {

        // TODO should be used without wrapper class
        private List<PartitionedJobProperty> partitionedJobProperties;
    }

    @Getter
    @Setter
    public static class PartitionedJobProperty {

        private String jobName;
        private Integer chunkSize;
        private Integer partitionSize;
        private Integer threadPoolCorePoolSize;
        private Integer threadPoolMaxPoolSize;
        private Integer threadPoolQueueCapacity;
        private Integer retryLimit;
        private Integer pollInterval;

    }

    @Getter
    @Setter
    public static class CorevanceRemoteJobMessageHandlerProperties {

        private CorevanceRemoteJobMessageHandlerSpringEventsProperties springEvents;
        private CorevanceRemoteJobMessageHandlerJmsProperties jms;
        private CorevanceRemoteJobMessageHandlerKafkaProperties kafka;
    }

    @Getter
    @Setter
    public static class CorevanceRemoteJobMessageHandlerSpringEventsProperties {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class CorevanceRemoteJobMessageHandlerJmsProperties {

        private boolean enabled;
        private String requestQueueName;
        private String brokerUrl;
        private String brokerUsername;
        private String brokerPassword;

        public boolean isBrokerPasswordProtected() {
            return StringUtils.isNotBlank(brokerUsername) || StringUtils.isNotBlank(brokerPassword);
        }
    }

    @Getter
    @Setter
    public static class CorevanceRemoteJobMessageHandlerKafkaProperties {

        private boolean enabled;
        private String bootstrapServers;
        private KafkaTopicProperties topic;
        private KafkaConsumerProperties consumer;
        private KafkaProperties producer;
        private KafkaProperties admin;
    }

    @Getter
    @Setter
    public static class KafkaTopicProperties {

        private boolean autoCreate;
        private String name;
        private int replicas;
        private int partitions;
    }

    @Getter
    @Setter
    public static class KafkaConsumerProperties extends KafkaProperties {

        private String groupId;
    }

    @Getter
    @Setter
    @Slf4j
    public static class KafkaProperties {

        private String extraPropertiesKeyValueSeparator;
        private String extraPropertiesSeparator;
        private String extraProperties;

        public Map<String, String> getExtraPropertiesMap() {
            Map<String, String> map = new HashMap<>();
            if (StringUtils.isNotEmpty(getExtraProperties()) && validateSeparators()) {
                String[] lines = StringUtils.split(getExtraProperties(), extraPropertiesSeparator);
                Arrays.stream(lines).forEach(line -> {
                    String[] keyAndValue = StringUtils.split(line, extraPropertiesKeyValueSeparator);
                    if (keyAndValue.length == 2) {
                        map.put(keyAndValue[0], keyAndValue[1]);
                    } else {
                        log.warn("Invalid property: {}", line);
                    }

                });
            }
            return map;
        }

        private boolean validateSeparators() {
            boolean valid = (StringUtils.isNotEmpty(extraPropertiesSeparator) && extraPropertiesSeparator.length() == 1
                    && StringUtils.isNotEmpty(extraPropertiesKeyValueSeparator) && extraPropertiesKeyValueSeparator.length() == 1
                    && !extraPropertiesSeparator.equals(extraPropertiesKeyValueSeparator));
            if (!valid) {
                log.warn("Invalid KafkaProperties configuration, lineSeparator '{}' and keyValueSeparator '{}'", extraPropertiesSeparator,
                        extraPropertiesKeyValueSeparator);
            }
            return valid;
        }
    }

    @Getter
    @Setter
    public static class CorevanceEventsProperties {

        private CorevanceExternalEventsProperties external;
    }

    @Getter
    @Setter
    public static class CorevanceTaskExecutor {

        private int defaultTaskExecutorCorePoolSize;
        private int defaultTaskExecutorMaxPoolSize;
        private int tenantUpgradeTaskExecutorCorePoolSize;
        private int tenantUpgradeTaskExecutorMaxPoolSize;
        private int tenantUpgradeTaskExecutorQueueCapacity;
    }

    @Getter
    @Setter
    public static class CorevanceExternalEventsProperties {

        private boolean enabled;
        private CorevanceExternalEventsProducerProperties producer;
        private int partitionSize;
        private int threadPoolCorePoolSize;
        private int threadPoolMaxPoolSize;
        private int threadPoolQueueCapacity;
    }

    @Getter
    @Setter
    public static class CorevanceExternalEventsProducerProperties {

        private CorevanceExternalEventsProducerJmsProperties jms;
        private CorevanceExternalEventsProducerKafkaProperties kafka;
    }

    @Getter
    @Setter
    public static class CorevanceExternalEventsProducerJmsProperties {

        private boolean enabled;
        private String eventQueueName;
        private String eventTopicName;
        private String brokerUrl;
        private String brokerUsername;
        private String brokerPassword;
        private int producerCount;
        private boolean asyncSendEnabled;
        private int threadPoolTaskExecutorCorePoolSize;
        private int threadPoolTaskExecutorMaxPoolSize;

        public boolean isBrokerPasswordProtected() {
            return StringUtils.isNotBlank(brokerUsername) || StringUtils.isNotBlank(brokerPassword);
        }
    }

    @Getter
    @Setter
    public static class CorevanceExternalEventsProducerKafkaProperties {

        private boolean enabled;
        private String bootstrapServers;
        private KafkaTopicProperties topic;
        private KafkaProperties producer;
        private KafkaProperties admin;
        private int timeoutInSeconds;
    }

    @Getter
    @Setter
    public static class CorevanceContentProperties {

        private boolean regexWhitelistEnabled;
        private List<String> regexWhitelist;
        private boolean mimeWhitelistEnabled;
        private List<String> mimeWhitelist;
        private Integer defaultBufferSize;
        private CorevanceContentFilesystemProperties filesystem;
        private CorevanceContentS3Properties s3;
    }

    @Getter
    @Setter
    public static class CorevanceContentFilesystemProperties {

        private Boolean enabled;
        private String rootFolder;
    }

    @Getter
    @Setter
    public static class CorevanceContentS3Properties {

        private Boolean enabled;
        private String bucketName;
        private String accessKey;
        private String secretKey;
        private String region;
        private String endpoint;
        private Boolean pathStyleAddressingEnabled;
    }

    @Getter
    @Setter
    public static class CorevanceReportProperties {

        private CorevanceExportProperties export;
    }

    @Getter
    @Setter
    public static class CorevanceExportProperties {

        private CorevanceExportS3Properties s3;
    }

    @Getter
    @Setter
    public static class CorevanceExportS3Properties {

        private String bucketName;
        private Boolean enabled;
    }

    @Getter
    @Setter
    public static class CorevanceJobProperties {

        private int stuckRetryThreshold;
        private boolean loanCobEnabled;
        private CorevanceJournalEntryAggregationProperties journalEntryAggregation;
        private int retainedEarningChunkSize;
    }

    @Getter
    @Setter
    public static class CorevanceJournalEntryAggregationProperties {

        private Integer excludeRecentNDays;
        private boolean enabled;
        private Integer chunkSize;
    }

    @Getter
    @Setter
    public static class CorevanceTemplateProperties {

        private boolean regexWhitelistEnabled;
        private List<String> regexWhitelist;
    }

    @Getter
    @Setter
    public static class CorevanceJpaProperties {

        private boolean statementLoggingEnabled;
    }

    @Getter
    @Setter
    public static class CorevanceDatabaseProperties {

        private String defaultMasterPassword;
    }

    @Getter
    @Setter
    public static class CorevanceQueryProperties {

        private int inClauseParameterSizeLimit;
    }

    @Getter
    @Setter
    public static class CorevanceApiProperties {

        private CorevanceBodyItemSizeLimitProperties bodyItemSizeLimit;
    }

    @Getter
    @Setter
    public static class CorevanceBodyItemSizeLimitProperties {

        private int inlineLoanCob;
    }

    @Getter
    @Setter
    public static class CorevanceNotificationProperties {

        private UserNotificationSystemProperties userNotificationSystem;
    }

    @Getter
    @Setter
    public static class UserNotificationSystemProperties {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class CorevanceLoanProperties {

        private CorevanceTransactionProcessorProperties transactionProcessor;
        private String statusChangeHistoryStatuses;
    }

    @Getter
    @Setter
    public static class CorevanceTransactionProcessorProperties {

        private CorevanceTransactionProcessorItemProperties creocore;
        private CorevanceTransactionProcessorItemProperties earlyRepayment;
        private CorevanceTransactionProcessorItemProperties mifosStandard;
        private CorevanceTransactionProcessorItemProperties heavensFamily;
        private CorevanceTransactionProcessorItemProperties interestPrincipalPenaltiesFees;
        private CorevanceTransactionProcessorItemProperties principalInterestPenaltiesFees;
        private CorevanceTransactionProcessorItemProperties rbiIndia;
        private CorevanceTransactionProcessorItemProperties duePenaltyFeeInterestPrincipalInAdvancePrincipalPenaltyFeeInterest;
        private CorevanceTransactionProcessorItemProperties duePenaltyInterestPrincipalFeeInAdvancePenaltyInterestPrincipalFee;
        private CorevanceTransactionProcessorItemProperties advancedPaymentStrategy;
        private boolean errorNotFoundFail;
    }

    @Getter
    @Setter
    public static class CorevanceSecurityProperties {

        private CorevanceSecurityBasicAuth basicauth;
        private CorevanceSecurityTwoFactorAuth twoFactor;
        private CorevanceSecurityHsts hsts;
        private CorevanceSecurityOAuth2Properties oauth2;
        private CorevanceSecurityOidcFederationProperties oidcFederation;
        private CorsProperties cors;

        public void set2fa(CorevanceSecurityTwoFactorAuth twoFactor) {
            this.twoFactor = twoFactor;
        }

        @Getter
        @Setter
        public static class CorevanceSecurityOAuth2Properties {

            private boolean enabled;
            private ClientProperties client;

            @Getter
            @Setter
            public static class ClientProperties implements Serializable {

                @Serial
                private static final long serialVersionUID = 1L;
                private Map<String, Registration> registrations = new HashMap<>();

                @Getter
                @Setter
                public static final class Registration implements Serializable {

                    @Serial
                    private static final long serialVersionUID = 1L;
                    private String clientId;
                    private List<String> scopes = new ArrayList<>();
                    private List<String> authorizationGrantTypes = new ArrayList<>();
                    private List<String> redirectUris = new ArrayList<>();
                    private boolean requireAuthorizationConsent = true;
                }
            }
        }

        @Getter
        @Setter
        public static class CorevanceSecurityOidcFederationProperties {

            private boolean enabled;
            // JWT claim name used to resolve the Corevance tenant ID.
            // Falls back to HTTP header / query param if absent.
            private String tenantClaimName = "corevance_tenant";
            // Claim used as the Corevance username. Common values: preferred_username, email, sub.
            private String usernameClaim = "preferred_username";
            // When true, creates a Corevance AppUser on first successful OIDC login.
            private boolean autoCreateUser = false;
            // Comma-separated role names assigned to auto-created users.
            private String defaultRoles = "";
            // Controls the RP-Initiated Logout URL format.
            // Values: keycloak | azure_ad | okta | auth0 | generic (default)
            private OidcFederationType provider = OidcFederationType.GENERIC;
            // Redirect URI sent to the IdP after successful logout.
            private String postLogoutRedirectUri;
            // Static per-issuer tenant mapping (YAML fallback).
            // Used when the master DB has no m_tenant_oidc_config record for an incoming issuer.
            // Priority: DB config > issuers[] > tenantClaimName claim.
            private List<OidcIssuerProperties> issuers = new ArrayList<>();

            @Getter
            @Setter
            public static class OidcIssuerProperties {

                // Exact value expected in the JWT 'iss' claim.
                private String issuerUri;
                // Corevance tenant identifier this issuer maps to.
                private String tenantId;
                // Optional: if absent, derived from issuerUri via OIDC discovery.
                private String jwksUri;
                // Optional: per-issuer override for the username claim.
                private String usernameClaim;
            }
        }

        @Getter
        @Setter
        public static class CorevanceSecurityBasicAuth {

            private boolean enabled;
        }

        @Getter
        @Setter
        public static class CorevanceSecurityTwoFactorAuth {

            private boolean enabled;
        }

        @Getter
        @Setter
        public static class CorevanceSecurityHsts {

            private boolean enabled;
        }
    }

    @Getter
    @Setter
    public static class CorevanceTransactionProcessorItemProperties {

        private boolean enabled;
    }

    @Getter
    @Setter
    public static class CorevanceSamplingProperties {

        private boolean enabled;
        private int samplingRate;
        private String sampledClasses;
        private int resetPeriodSec;
    }

    @Getter
    @Setter
    public static class CorevanceModulesProperties {

        private CorevanceInvestorModuleProperties investor;
        private CorevanceLoanOriginationModuleProperties loanOrigination;
    }

    @Getter
    @Setter
    public static class CorevanceInvestorModuleProperties extends AbstractCorevanceModuleProperties {

    }

    @Getter
    @Setter
    public static class CorevanceLoanOriginationModuleProperties extends AbstractCorevanceModuleProperties {

    }

    @Getter
    @Setter
    public static class CorevanceSqlValidationProperties {

        private List<CorevanceSqlValidationPatternProperties> patterns;
        private List<CorevanceSqlValidationProfileProperties> profiles;
    }

    @Getter
    @Setter
    public static class CorevanceSqlValidationProfileProperties {

        private String name;
        private String description;
        private List<CorevanceSqlValidationPatternReferenceProperties> patternRefs;
        private Boolean enabled = true;
    }

    @Getter
    @Setter
    public static class CorevanceSqlValidationPatternReferenceProperties {

        private String name;
        private Integer order;
    }

    @Getter
    @Setter
    public static class CorevanceSqlValidationPatternProperties {

        private String name;
        private String pattern;
    }

    @Getter
    @Setter
    public static class CorevanceInputValidationProperties {

        private List<CorevanceInputValidationPatternProperties> patterns;
        private List<CorevanceInputValidationProfileProperties> profiles;
    }

    @Getter
    @Setter
    public static class CorevanceInputValidationProfileProperties {

        private String name;
        private String description;
        private List<CorevanceInputValidationPatternReferenceProperties> patternRefs;
        private Boolean enabled = true;
    }

    @Getter
    @Setter
    public static class CorevanceInputValidationPatternReferenceProperties {

        private String name;
        private Integer order;
    }

    @Getter
    @Setter
    public static class CorevanceInputValidationPatternProperties {

        private String name;
        private String pattern;
    }

    @Getter
    @Setter
    public static class CorevanceCache {

        private CorevanceCacheDetails defaultTemplate;
        private Map<String, CorevanceCacheDetails> customTemplates = new HashMap<>();
    }

    @Getter
    @Setter
    public static class CorevanceCacheDetails {

        private Duration ttl;
        private Integer maximumEntries;
    }

    @Setter
    @Getter
    public static class RetryProperties {

        private InstancesProperties instances;

        @Setter
        @Getter
        public static class InstancesProperties {

            private ExecuteCommandProperties executeCommand;

            @Getter
            @Setter
            public static class ExecuteCommandProperties {

                private Class<? extends Throwable>[] retryExceptions;
                private Integer maxAttempts;
                private Boolean enableExponentialBackoff;
                private Double exponentialBackoffMultiplier;
                private Duration waitDuration;

            }
        }
    }

    @Getter
    @Setter
    public static class CorsProperties {

        private boolean enabled;
        private List<String> allowedOriginPatterns;
        private List<String> allowedMethods;
        private List<String> allowedHeaders;
        private List<String> exposedHeaders;
        private boolean allowCredentials;
    }

    @Getter
    @Setter
    public static class CorevanceDefaultValues {

        private Long officeId;
    }
}
