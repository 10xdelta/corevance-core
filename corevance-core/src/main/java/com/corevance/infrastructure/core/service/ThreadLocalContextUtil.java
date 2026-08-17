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
package com.corevance.infrastructure.core.service;

import java.time.LocalDate;
import java.util.HashMap;
import com.corevance.infrastructure.businessdate.domain.BusinessDateType;
import com.corevance.infrastructure.core.domain.ActionContext;
import com.corevance.infrastructure.core.domain.CorevanceContext;
import com.corevance.infrastructure.core.domain.CorevancePlatformTenant;
import org.springframework.util.Assert;

/**
 * A utility class for managing ThreadLocal context in the application. Provides methods for context initialization and
 * cleanup.
 */
public final class ThreadLocalContextUtil {

    public static final String CONTEXT_TENANTS = "tenants";
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();
    private static final ThreadLocal<CorevancePlatformTenant> tenantContext = new ThreadLocal<>();
    private static final ThreadLocal<String> authTokenContext = new ThreadLocal<>();
    private static final ThreadLocal<HashMap<BusinessDateType, LocalDate>> businessDateContext = new ThreadLocal<>();
    private static final ThreadLocal<ActionContext> actionContext = new ThreadLocal<>();

    private ThreadLocalContextUtil() {}

    public static CorevancePlatformTenant getTenant() {
        return tenantContext.get();
    }

    public static void setTenant(final CorevancePlatformTenant tenant) {
        tenantContext.set(tenant);
    }

    public static void clearTenant() {
        tenantContext.remove();
    }

    public static String getDataSourceContext() {
        return contextHolder.get();
    }

    public static void setDataSourceContext(final String dataSourceContext) {
        contextHolder.set(dataSourceContext);
    }

    public static void clearDataSourceContext() {
        contextHolder.remove();
    }

    public static String getAuthToken() {
        return authTokenContext.get();
    }

    public static void setAuthToken(final String authToken) {
        authTokenContext.set(authToken);
    }

    // Map is not serializable, but Hashmap is
    public static HashMap<BusinessDateType, LocalDate> getBusinessDates() {
        Assert.notNull(businessDateContext.get(), "Business dates cannot be null!");
        return businessDateContext.get();
    }

    public static void setBusinessDates(HashMap<BusinessDateType, LocalDate> dates) {
        Assert.notNull(dates, "Business dates cannot be null!");
        businessDateContext.set(dates);
    }

    public static LocalDate getBusinessDateByType(BusinessDateType businessDateType) {
        Assert.notNull(businessDateType, "Business date type cannot be null!");
        LocalDate localDate = getBusinessDates().get(businessDateType);
        Assert.notNull(localDate, String.format("Business date with type `%s` is not initialised!", businessDateType));
        return localDate;
    }

    public static LocalDate getBusinessDate() {
        BusinessDateType businessDateType = getActionContext().getBusinessDateType();
        return getBusinessDateByType(businessDateType);
    }

    public static ActionContext getActionContext() {
        return actionContext.get() == null ? ActionContext.DEFAULT : actionContext.get();
    }

    public static void setActionContext(ActionContext context) {
        Assert.notNull(context, "context cannot be null");
        actionContext.set(context);
    }

    public static CorevanceContext getContext() {
        return new CorevanceContext(getDataSourceContext(), getTenant(), getAuthToken(), getBusinessDates(), getActionContext());
    }

    public static void init(final CorevanceContext corevanceContext) {
        Assert.notNull(corevanceContext, "CorevanceContext cannot be null during synchronisation!");
        setDataSourceContext(corevanceContext.getContextHolder());
        setTenant(corevanceContext.getTenantContext());
        setAuthToken(corevanceContext.getAuthTokenContext());
        setBusinessDates(corevanceContext.getBusinessDateContext());
        setActionContext(corevanceContext.getActionContext());
    }

    public static void reset() {
        contextHolder.remove();
        tenantContext.remove();
        authTokenContext.remove();
        businessDateContext.remove();
        actionContext.remove();
    }

}
