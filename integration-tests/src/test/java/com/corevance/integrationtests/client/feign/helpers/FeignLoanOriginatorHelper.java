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
package com.corevance.integrationtests.client.feign.helpers;

import static com.corevance.client.feign.util.FeignCalls.fail;
import static com.corevance.client.feign.util.FeignCalls.ok;

import java.util.List;
import java.util.UUID;
import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.feign.util.CallFailedRuntimeException;
import com.corevance.client.models.GetLoanOriginatorTemplateResponse;
import com.corevance.client.models.GetLoanOriginatorsResponse;
import com.corevance.client.models.PostLoanOriginatorsRequest;
import com.corevance.client.models.PostLoanOriginatorsResponse;
import com.corevance.client.models.PutLoanOriginatorsRequest;
import com.corevance.client.models.PutLoanOriginatorsResponse;

public class FeignLoanOriginatorHelper {

    private final CorevanceFeignClient corevanceClient;

    public FeignLoanOriginatorHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public Long createOriginator(String externalId) {
        return createOriginator(new PostLoanOriginatorsRequest().externalId(externalId).name(externalId));
    }

    public Long createOriginator(String externalId, String name, String status) {
        return createOriginator(new PostLoanOriginatorsRequest().externalId(externalId).name(name).status(status));
    }

    public Long createOriginator(PostLoanOriginatorsRequest request) {
        PostLoanOriginatorsResponse response = ok(() -> corevanceClient.loanOriginators().createLoanOriginator(request));
        return response.getResourceId();
    }

    public CallFailedRuntimeException createOriginatorExpectingError(PostLoanOriginatorsRequest request) {
        return fail(() -> corevanceClient.loanOriginators().createLoanOriginator(request));
    }

    public List<GetLoanOriginatorsResponse> getAllOriginators() {
        return ok(() -> corevanceClient.loanOriginators().retrieveAllLoanOriginators());
    }

    public GetLoanOriginatorsResponse getOriginatorById(Long originatorId) {
        return ok(() -> corevanceClient.loanOriginators().retrieveOneLoanOriginator(originatorId));
    }

    public CallFailedRuntimeException getOriginatorByIdExpectingError(Long originatorId) {
        return fail(() -> corevanceClient.loanOriginators().retrieveOneLoanOriginator(originatorId));
    }

    public GetLoanOriginatorsResponse getOriginatorByExternalId(String externalId) {
        return ok(() -> corevanceClient.loanOriginators().retrieveByExternalId(externalId));
    }

    public CallFailedRuntimeException getOriginatorByExternalIdExpectingError(String externalId) {
        return fail(() -> corevanceClient.loanOriginators().retrieveByExternalId(externalId));
    }

    public PutLoanOriginatorsResponse updateOriginator(Long originatorId, PutLoanOriginatorsRequest request) {
        return ok(() -> corevanceClient.loanOriginators().updateLoanOriginator(originatorId, request));
    }

    public PutLoanOriginatorsResponse updateOriginatorByExternalId(String externalId, PutLoanOriginatorsRequest request) {
        return ok(() -> corevanceClient.loanOriginators().updateByExternalId(externalId, request));
    }

    public CallFailedRuntimeException updateOriginatorExpectingError(Long originatorId, PutLoanOriginatorsRequest request) {
        return fail(() -> corevanceClient.loanOriginators().updateLoanOriginator(originatorId, request));
    }

    public Long deleteOriginator(Long originatorId) {
        var response = ok(() -> corevanceClient.loanOriginators().deleteLoanOriginator(originatorId));
        return response.getResourceId();
    }

    public Long deleteOriginatorByExternalId(String externalId) {
        var response = ok(() -> corevanceClient.loanOriginators().deleteByExternalId(externalId));
        return response.getResourceId();
    }

    public CallFailedRuntimeException deleteOriginatorExpectingError(Long originatorId) {
        return fail(() -> corevanceClient.loanOriginators().deleteLoanOriginator(originatorId));
    }

    public static String generateUniqueExternalId() {
        return "EXT-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public void attachOriginatorToLoan(Long loanId, Long originatorId) {
        ok(() -> {
            corevanceClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId);
            return null;
        });
    }

    public CallFailedRuntimeException attachOriginatorToLoanExpectingError(Long loanId, Long originatorId) {
        return fail(() -> {
            corevanceClient.loanOriginators().attachOriginatorToLoan(loanId, originatorId);
            return null;
        });
    }

    public void detachOriginatorFromLoan(Long loanId, Long originatorId) {
        ok(() -> {
            corevanceClient.loanOriginators().detachOriginatorFromLoan(loanId, originatorId);
            return null;
        });
    }

    public CallFailedRuntimeException detachOriginatorFromLoanExpectingError(Long loanId, Long originatorId) {
        return fail(() -> {
            corevanceClient.loanOriginators().detachOriginatorFromLoan(loanId, originatorId);
            return null;
        });
    }

    public GetLoanOriginatorTemplateResponse retrieveLoanOriginatorTemplate() {
        return ok(() -> corevanceClient.loanOriginators().retrieveLoanOriginatorTemplate());
    }
}
