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
package com.corevance.infrastructure.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import javax.sql.DataSource;
import com.corevance.infrastructure.core.config.CorevanceProperties;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenant;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenantConnection;
import com.corevance.infrastructure.core.service.database.DataSourcePerTenantServiceFactory;
import com.corevance.infrastructure.core.service.database.DatabasePasswordEncryptor;
import com.corevance.infrastructure.core.service.database.HikariDataSourceFactory;
import com.corevance.infrastructure.security.utils.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCrypt;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DataSourcePerTenantServiceFactoryTest {

    public static final String MASTER_DB_SERVER = "localhost";
    public static final String MASTER_DB_SERVER_PORT = "5432";
    public static final String MASTER_DB_SCHEMA_NAME = "corevance_tenants";
    public static final String MASTER_DB_USERNAME = "root";
    public static final String MASTER_DB_PASSWORD = "password";
    public static final String MASTER_DB_CONN_PARAMS = "something";
    public static final String MASTER_DB_JDBC_URL = "jdbc:postgresql://" + MASTER_DB_SERVER + ":" + MASTER_DB_SERVER_PORT + "/"
            + MASTER_DB_SCHEMA_NAME + "?" + MASTER_DB_CONN_PARAMS;

    public static final String READONLY_DB_SERVER = "localhost-readonly";
    public static final String READONLY_DB_SERVER_PORT = "5432-readonly";
    public static final String READONLY_DB_SCHEMA_NAME = "corevance_tenants-readonly";
    public static final String READONLY_DB_USERNAME = "root-readonly";
    public static final String READONLY_DB_PASSWORD = "password-readonly";
    public static final String READONLY_DB_CONN_PARAMS = "something-readonly";
    public static final String READONLY_DB_JDBC_URL = "jdbc:postgresql://" + READONLY_DB_SERVER + ":" + READONLY_DB_SERVER_PORT + "/"
            + READONLY_DB_SCHEMA_NAME + "?" + READONLY_DB_CONN_PARAMS;

    public static final int MASTER_DB_INITIAL_SIZE = 1;
    public static final int MASTER_DB_MAX_ACTIVE = 5;
    public static final long MASTER_DB_VALIDATION_INTERVAL = 500L;
    public static final long MASTER_DB_INIT_FAIL_TIMEOUT = 0L;
    public static final long MASTER_DB_LEAK_DETECTION_THRESHOLD = 20_000L;

    public static final String MASTER_DB_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    public static final String MASTER_DB_CONN_TEST_QUERY = "SELECT 1";
    public static final boolean MASTER_DB_AUTO_COMMIT_ENABLED = true;

    public static final String MASTER_MASTER_PASSWORD = "corevance";

    public static final String MASTER_ENCRYPTION = "AES/CBC/PKCS5Padding";
    public static final CorevancePlatformTenant TENANT = new CorevancePlatformTenant(1L, "", "", "", null);

    @Mock
    private CorevanceProperties corevanceProperties;

    @Mock
    private HikariConfig tenantHikariConfig;

    @Mock
    private CorevancePlatformTenant defaultTenant;

    @Mock
    private CorevancePlatformTenantConnection tenantConnection;

    @Mock
    private DataSource tenantDataSource;

    @Mock
    private HikariDataSourceFactory hikariDataSourceFactory;

    @Captor
    private ArgumentCaptor<HikariConfig> hikariConfigCaptor;

    @Mock
    private DatabasePasswordEncryptor databasePasswordEncryptor;

    @Mock
    private Optional<MeterRegistry> meterRegistry;

    @InjectMocks
    private DataSourcePerTenantServiceFactory underTest;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = mock(Connection.class);
        given(tenantDataSource.getConnection()).willReturn(connection);

        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        given(connection.getMetaData()).willReturn(databaseMetaData);

        given(databaseMetaData.getURL()).willReturn("jdbc:postgresql://localhost:5432/corevance_tenants");

        given(tenantConnection.getSchemaServer()).willReturn(MASTER_DB_SERVER);
        given(tenantConnection.getSchemaServerPort()).willReturn(MASTER_DB_SERVER_PORT);
        given(tenantConnection.getSchemaName()).willReturn(MASTER_DB_SCHEMA_NAME);
        given(tenantConnection.getSchemaUsername()).willReturn(MASTER_DB_USERNAME);
        given(tenantConnection.getSchemaPassword())
                .willReturn(EncryptionUtil.encryptToBase64(MASTER_ENCRYPTION, MASTER_MASTER_PASSWORD, MASTER_DB_PASSWORD));
        given(tenantConnection.getSchemaConnectionParameters()).willReturn(MASTER_DB_CONN_PARAMS);

        given(tenantConnection.getReadOnlySchemaServer()).willReturn(READONLY_DB_SERVER);
        given(tenantConnection.getReadOnlySchemaServerPort()).willReturn(READONLY_DB_SERVER_PORT);
        given(tenantConnection.getReadOnlySchemaName()).willReturn(READONLY_DB_SCHEMA_NAME);
        given(tenantConnection.getReadOnlySchemaUsername()).willReturn(READONLY_DB_USERNAME);
        given(tenantConnection.getReadOnlySchemaPassword())
                .willReturn(EncryptionUtil.encryptToBase64(MASTER_ENCRYPTION, MASTER_MASTER_PASSWORD, READONLY_DB_PASSWORD));
        given(tenantConnection.getReadOnlySchemaConnectionParameters()).willReturn(READONLY_DB_CONN_PARAMS);
        String hashedMasterPassword = BCrypt.hashpw("master-password", BCrypt.gensalt());
        given(tenantConnection.getMasterPasswordHash()).willReturn(hashedMasterPassword);

        given(defaultTenant.getConnection()).willReturn(tenantConnection);
        given(tenantConnection.getInitialSize()).willReturn(MASTER_DB_INITIAL_SIZE);
        given(tenantConnection.getMaxActive()).willReturn(MASTER_DB_MAX_ACTIVE);
        given(tenantConnection.getValidationInterval()).willReturn(MASTER_DB_VALIDATION_INTERVAL);

        given(tenantHikariConfig.getInitializationFailTimeout()).willReturn(MASTER_DB_INIT_FAIL_TIMEOUT);
        given(tenantHikariConfig.getDriverClassName()).willReturn(MASTER_DB_DRIVER_CLASS_NAME);
        given(tenantHikariConfig.getConnectionTestQuery()).willReturn(MASTER_DB_CONN_TEST_QUERY);
        given(tenantHikariConfig.getDataSourceProperties()).willReturn(mock(Properties.class));
        given(tenantHikariConfig.isAutoCommit()).willReturn(MASTER_DB_AUTO_COMMIT_ENABLED);
        given(tenantHikariConfig.getLeakDetectionThreshold()).willReturn(0L);

        given(hikariDataSourceFactory.create(any())).willReturn(mock(HikariDataSource.class));

        CorevanceProperties.CorevanceConfigProperties configProperties = new CorevanceProperties.CorevanceConfigProperties();
        configProperties.setMinPoolSize(-1);
        configProperties.setMaxPoolSize(-1);
        configProperties.setLeakDetectionThreshold(0L);

        CorevanceProperties.CorevanceTenantProperties tenantPropertiesMock = mock(CorevanceProperties.CorevanceTenantProperties.class);
        given(tenantPropertiesMock.getEncryption()).willReturn(MASTER_ENCRYPTION);
        given(tenantPropertiesMock.getMasterPassword()).willReturn(MASTER_MASTER_PASSWORD);

        given(tenantPropertiesMock.getConfig()).willReturn(configProperties);
        given(corevanceProperties.getTenant()).willReturn(tenantPropertiesMock);

        given(databasePasswordEncryptor.isMasterPasswordHashValid(any())).willReturn(true);
        given(databasePasswordEncryptor.getMasterPasswordHash()).willReturn(hashedMasterPassword);
        given(databasePasswordEncryptor.decrypt(any())).will(
                answer -> EncryptionUtil.decryptFromBase64(MASTER_ENCRYPTION, MASTER_MASTER_PASSWORD, answer.getArgument(0, String.class)));
        given(databasePasswordEncryptor.encrypt(any())).will(
                answer -> EncryptionUtil.encryptToBase64(MASTER_ENCRYPTION, MASTER_MASTER_PASSWORD, answer.getArgument(0, String.class)));
    }

    @Test
    void testCreateNewDataSourceFor_ShouldUseNormalConfiguration_WhenInAllMode() {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = createModeProps(MASTER_DB_AUTO_COMMIT_ENABLED,
                MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED);
        given(corevanceProperties.getMode()).willReturn(modeProperties);

        // when
        DataSource dataSource = underTest.createNewDataSourceFor(TENANT, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
        verify(hikariDataSourceFactory).create(hikariConfigCaptor.capture());
        HikariConfig hikariConfig = hikariConfigCaptor.getValue();
        assertFalse(hikariConfig.isReadOnly());
        assertEquals(MASTER_DB_JDBC_URL, hikariConfig.getJdbcUrl());
        assertEquals(MASTER_DB_SCHEMA_NAME + "_pool", hikariConfig.getPoolName());
        assertEquals(MASTER_DB_USERNAME, hikariConfig.getUsername());
        assertEquals(MASTER_DB_PASSWORD, hikariConfig.getPassword());
        assertEquals(MASTER_DB_INITIAL_SIZE, hikariConfig.getMinimumIdle());
        assertEquals(MASTER_DB_MAX_ACTIVE, hikariConfig.getMaximumPoolSize());
        assertEquals(MASTER_DB_VALIDATION_INTERVAL, hikariConfig.getValidationTimeout());
        assertEquals(MASTER_DB_DRIVER_CLASS_NAME, hikariConfig.getDriverClassName());
        assertEquals(MASTER_DB_CONN_TEST_QUERY, hikariConfig.getConnectionTestQuery());
        assertEquals(MASTER_DB_AUTO_COMMIT_ENABLED, hikariConfig.isAutoCommit());
        assertEquals(0L, hikariConfig.getLeakDetectionThreshold());
    }

    @Test
    void testCreateNewDataSourceFor_ShouldOverridesMinPoolConfiguration_WhenConfigured() {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = createModeProps(MASTER_DB_AUTO_COMMIT_ENABLED,
                MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED);
        given(corevanceProperties.getMode()).willReturn(modeProperties);

        int minPoolSize = 10;

        CorevanceProperties.CorevanceConfigProperties config = corevanceProperties.getTenant().getConfig();
        config.setMinPoolSize(minPoolSize);

        // when
        DataSource dataSource = underTest.createNewDataSourceFor(TENANT, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
        verify(hikariDataSourceFactory).create(hikariConfigCaptor.capture());
        HikariConfig hikariConfig = hikariConfigCaptor.getValue();
        assertFalse(hikariConfig.isReadOnly());
        assertEquals(MASTER_DB_JDBC_URL, hikariConfig.getJdbcUrl());
        assertEquals(MASTER_DB_SCHEMA_NAME + "_pool", hikariConfig.getPoolName());
        assertEquals(MASTER_DB_USERNAME, hikariConfig.getUsername());
        assertEquals(MASTER_DB_PASSWORD, hikariConfig.getPassword());
        assertEquals(minPoolSize, hikariConfig.getMinimumIdle());
        assertEquals(MASTER_DB_MAX_ACTIVE, hikariConfig.getMaximumPoolSize());
        assertEquals(MASTER_DB_VALIDATION_INTERVAL, hikariConfig.getValidationTimeout());
        assertEquals(MASTER_DB_DRIVER_CLASS_NAME, hikariConfig.getDriverClassName());
        assertEquals(MASTER_DB_CONN_TEST_QUERY, hikariConfig.getConnectionTestQuery());
        assertEquals(MASTER_DB_AUTO_COMMIT_ENABLED, hikariConfig.isAutoCommit());
    }

    @Test
    void testCreateNewDataSourceFor_ShouldUseMasterLeakDetectionThreshold_WhenConfiguredOnHikari() {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = createModeProps(MASTER_DB_AUTO_COMMIT_ENABLED,
                MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED);
        given(corevanceProperties.getMode()).willReturn(modeProperties);
        given(tenantHikariConfig.getLeakDetectionThreshold()).willReturn(MASTER_DB_LEAK_DETECTION_THRESHOLD);

        // when
        DataSource dataSource = underTest.createNewDataSourceFor(TENANT, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
        verify(hikariDataSourceFactory).create(hikariConfigCaptor.capture());
        HikariConfig hikariConfig = hikariConfigCaptor.getValue();
        assertEquals(MASTER_DB_LEAK_DETECTION_THRESHOLD, hikariConfig.getLeakDetectionThreshold());
    }

    @Test
    void testCreateNewDataSourceFor_ShouldOverrideLeakDetectionThreshold_WhenConfigured() {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = createModeProps(MASTER_DB_AUTO_COMMIT_ENABLED,
                MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED);
        given(corevanceProperties.getMode()).willReturn(modeProperties);

        CorevanceProperties.CorevanceConfigProperties config = corevanceProperties.getTenant().getConfig();
        config.setLeakDetectionThreshold(MASTER_DB_LEAK_DETECTION_THRESHOLD);

        // when
        DataSource dataSource = underTest.createNewDataSourceFor(TENANT, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
        verify(hikariDataSourceFactory).create(hikariConfigCaptor.capture());
        HikariConfig hikariConfig = hikariConfigCaptor.getValue();
        assertEquals(MASTER_DB_LEAK_DETECTION_THRESHOLD, hikariConfig.getLeakDetectionThreshold());
    }

    @Test
    void testCreateNewDataSourceFor_ShouldOverridesMaxPoolConfiguration_WhenConfigured() {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = createModeProps(MASTER_DB_AUTO_COMMIT_ENABLED,
                MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED);
        given(corevanceProperties.getMode()).willReturn(modeProperties);

        int maxPoolSize = 10;

        CorevanceProperties.CorevanceConfigProperties config = corevanceProperties.getTenant().getConfig();
        config.setMaxPoolSize(maxPoolSize);

        // when
        DataSource dataSource = underTest.createNewDataSourceFor(TENANT, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
        verify(hikariDataSourceFactory).create(hikariConfigCaptor.capture());
        HikariConfig hikariConfig = hikariConfigCaptor.getValue();
        assertFalse(hikariConfig.isReadOnly());
        assertEquals(MASTER_DB_JDBC_URL, hikariConfig.getJdbcUrl());
        assertEquals(MASTER_DB_SCHEMA_NAME + "_pool", hikariConfig.getPoolName());
        assertEquals(MASTER_DB_USERNAME, hikariConfig.getUsername());
        assertEquals(MASTER_DB_PASSWORD, hikariConfig.getPassword());
        assertEquals(MASTER_DB_INITIAL_SIZE, hikariConfig.getMinimumIdle());
        assertEquals(maxPoolSize, hikariConfig.getMaximumPoolSize());
        assertEquals(MASTER_DB_VALIDATION_INTERVAL, hikariConfig.getValidationTimeout());
        assertEquals(MASTER_DB_DRIVER_CLASS_NAME, hikariConfig.getDriverClassName());
        assertEquals(MASTER_DB_CONN_TEST_QUERY, hikariConfig.getConnectionTestQuery());
        assertEquals(MASTER_DB_AUTO_COMMIT_ENABLED, hikariConfig.isAutoCommit());
    }

    @Test
    void testCreateNewDataSourceFor_ShouldOverridesMinAndMaxPoolConfiguration_WhenBothConfigured() {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = createModeProps(MASTER_DB_AUTO_COMMIT_ENABLED,
                MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED, MASTER_DB_AUTO_COMMIT_ENABLED);
        given(corevanceProperties.getMode()).willReturn(modeProperties);

        int minPoolSize = 10;
        int maxPoolSize = 10;

        CorevanceProperties.CorevanceConfigProperties config = corevanceProperties.getTenant().getConfig();
        config.setMinPoolSize(minPoolSize);
        config.setMaxPoolSize(maxPoolSize);

        // when
        DataSource dataSource = underTest.createNewDataSourceFor(TENANT, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
        verify(hikariDataSourceFactory).create(hikariConfigCaptor.capture());
        HikariConfig hikariConfig = hikariConfigCaptor.getValue();
        assertFalse(hikariConfig.isReadOnly());
        assertEquals(MASTER_DB_JDBC_URL, hikariConfig.getJdbcUrl());
        assertEquals(MASTER_DB_SCHEMA_NAME + "_pool", hikariConfig.getPoolName());
        assertEquals(MASTER_DB_USERNAME, hikariConfig.getUsername());
        assertEquals(MASTER_DB_PASSWORD, hikariConfig.getPassword());
        assertEquals(minPoolSize, hikariConfig.getMinimumIdle());
        assertEquals(maxPoolSize, hikariConfig.getMaximumPoolSize());
        assertEquals(MASTER_DB_VALIDATION_INTERVAL, hikariConfig.getValidationTimeout());
        assertEquals(MASTER_DB_DRIVER_CLASS_NAME, hikariConfig.getDriverClassName());
        assertEquals(MASTER_DB_CONN_TEST_QUERY, hikariConfig.getConnectionTestQuery());
        assertEquals(MASTER_DB_AUTO_COMMIT_ENABLED, hikariConfig.isAutoCommit());
    }

    @Test
    void testCreateNewDataSourceFor_ShouldUseReadOnlyConfiguration_WhenInReadOnlyMode() {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = createModeProps(true, false, false, false);
        given(corevanceProperties.getMode()).willReturn(modeProperties);

        // when
        DataSource dataSource = underTest.createNewDataSourceFor(TENANT, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
        verify(hikariDataSourceFactory).create(hikariConfigCaptor.capture());
        HikariConfig hikariConfig = hikariConfigCaptor.getValue();
        assertTrue(hikariConfig.isReadOnly());
        assertEquals(READONLY_DB_JDBC_URL, hikariConfig.getJdbcUrl());
        assertEquals(READONLY_DB_SCHEMA_NAME + "_pool", hikariConfig.getPoolName());
        assertEquals(READONLY_DB_USERNAME, hikariConfig.getUsername());
        assertEquals(READONLY_DB_PASSWORD, hikariConfig.getPassword());
        assertEquals(MASTER_DB_INITIAL_SIZE, hikariConfig.getMinimumIdle());
        assertEquals(MASTER_DB_MAX_ACTIVE, hikariConfig.getMaximumPoolSize());
        assertEquals(MASTER_DB_VALIDATION_INTERVAL, hikariConfig.getValidationTimeout());
        assertEquals(MASTER_DB_DRIVER_CLASS_NAME, hikariConfig.getDriverClassName());
        assertEquals(MASTER_DB_CONN_TEST_QUERY, hikariConfig.getConnectionTestQuery());
        assertEquals(MASTER_DB_AUTO_COMMIT_ENABLED, hikariConfig.isAutoCommit());
    }

    @Test
    void testCreateNewDataSourceFor_ShouldUseNormalConfiguration_WhenInBatchOnlyMode() {
        // given
        CorevanceProperties.CorevanceModeProperties modeProperties = createModeProps(false, false, true, true);
        given(corevanceProperties.getMode()).willReturn(modeProperties);

        // when
        DataSource dataSource = underTest.createNewDataSourceFor(TENANT, defaultTenant.getConnection());

        // then
        assertNotNull(dataSource);
        verify(hikariDataSourceFactory).create(hikariConfigCaptor.capture());
        HikariConfig hikariConfig = hikariConfigCaptor.getValue();
        assertFalse(hikariConfig.isReadOnly());
        assertEquals(MASTER_DB_JDBC_URL, hikariConfig.getJdbcUrl());
        assertEquals(MASTER_DB_SCHEMA_NAME + "_pool", hikariConfig.getPoolName());
        assertEquals(MASTER_DB_USERNAME, hikariConfig.getUsername());
        assertEquals(MASTER_DB_PASSWORD, hikariConfig.getPassword());
        assertEquals(MASTER_DB_INITIAL_SIZE, hikariConfig.getMinimumIdle());
        assertEquals(MASTER_DB_MAX_ACTIVE, hikariConfig.getMaximumPoolSize());
        assertEquals(MASTER_DB_VALIDATION_INTERVAL, hikariConfig.getValidationTimeout());
        assertEquals(MASTER_DB_DRIVER_CLASS_NAME, hikariConfig.getDriverClassName());
        assertEquals(MASTER_DB_CONN_TEST_QUERY, hikariConfig.getConnectionTestQuery());
        assertEquals(MASTER_DB_AUTO_COMMIT_ENABLED, hikariConfig.isAutoCommit());
    }

    private CorevanceProperties.CorevanceModeProperties createModeProps(boolean readEnabled, boolean writeEnabled, boolean batchWorkerEnabled,
            boolean batchManagerEnabled) {
        CorevanceProperties.CorevanceModeProperties modeProperties = new CorevanceProperties.CorevanceModeProperties();
        modeProperties.setReadEnabled(readEnabled);
        modeProperties.setWriteEnabled(writeEnabled);
        modeProperties.setBatchWorkerEnabled(batchWorkerEnabled);
        modeProperties.setBatchManagerEnabled(batchManagerEnabled);
        return modeProperties;
    }

}
