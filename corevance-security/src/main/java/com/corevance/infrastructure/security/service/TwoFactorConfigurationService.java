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
package com.corevance.infrastructure.security.service;

import java.util.Map;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.security.data.OTPRequest;
import com.corevance.useradministration.domain.AppUser;

public interface TwoFactorConfigurationService {

    Map<String, Object> retrieveAll();

    boolean isSMSEnabled();

    Integer getSMSProviderId();

    String getSmsText();

    boolean isEmailEnabled();

    String getEmailSubject();

    String getEmailBody();

    String getFormattedEmailSubjectFor(AppUser user, OTPRequest request);

    String getFormattedEmailBodyFor(AppUser user, OTPRequest request);

    String getFormattedSmsTextFor(AppUser user, OTPRequest request);

    Integer getOTPTokenLength();

    Integer getOTPTokenLiveTime();

    Integer getAccessTokenLiveTime();

    Integer getAccessTokenExtendedLiveTime();

    Map<String, Object> update(JsonCommand command);
}
