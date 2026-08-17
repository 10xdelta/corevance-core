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

import com.corevance.client.feign.CorevanceFeignClient;
import com.corevance.client.feign.util.CallFailedRuntimeException;
import com.corevance.client.models.ChargeRequest;
import com.corevance.client.models.DeleteChargesChargeIdResponse;
import com.corevance.client.models.DeleteClientsClientIdChargesChargeIdResponse;
import com.corevance.client.models.GetChargesResponse;
import com.corevance.client.models.GetClientsClientIdChargesResponse;
import com.corevance.client.models.PostChargesResponse;
import com.corevance.client.models.PostClientsClientIdChargesChargeIdRequest;
import com.corevance.client.models.PostClientsClientIdChargesChargeIdResponse;
import com.corevance.client.models.PostClientsClientIdChargesRequest;
import com.corevance.client.models.PostClientsClientIdChargesResponse;
import com.corevance.client.models.PutChargesChargeIdResponse;
import com.corevance.integrationtests.client.feign.modules.ChargeRequestBuilders;

public class FeignChargesHelper {

    private static final String PAY_COMMAND = "paycharge";

    private final CorevanceFeignClient corevanceClient;

    public FeignChargesHelper(CorevanceFeignClient corevanceClient) {
        this.corevanceClient = corevanceClient;
    }

    public PostChargesResponse createCharge(ChargeRequest request) {
        return ok(() -> corevanceClient.charges().createCharge(request));
    }

    public GetChargesResponse getCharge(Long chargeId) {
        return ok(() -> corevanceClient.charges().retrieveOneCharge(chargeId));
    }

    public PutChargesChargeIdResponse updateCharge(Long chargeId, ChargeRequest request) {
        return ok(() -> corevanceClient.charges().updateCharge(chargeId, request));
    }

    public DeleteChargesChargeIdResponse deleteCharge(Long chargeId) {
        return ok(() -> corevanceClient.charges().deleteCharge(chargeId));
    }

    public CallFailedRuntimeException getChargeExpectingError(Long chargeId) {
        return fail(() -> corevanceClient.charges().retrieveOneCharge(chargeId));
    }

    public PostChargesResponse createLoanSpecifiedDueDateCharge(double amount) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(amount));
    }

    public PostChargesResponse createLoanSpecifiedDueDateCharge(double amount, String currencyCode) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDateFee(amount, currencyCode));
    }

    public PostChargesResponse createLoanDisbursementCharge(double amount) {
        return createCharge(ChargeRequestBuilders.loanDisbursementFee(amount));
    }

    public PostChargesResponse createLoanSpecifiedDueDatePenalty(double amount) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDatePenalty(amount));
    }

    public PostChargesResponse createLoanSpecifiedDueDatePercentageAmountAndInterestFee(double amount) {
        return createCharge(ChargeRequestBuilders.loanSpecifiedDueDatePercentageAmountAndInterestFee(amount));
    }

    public PostChargesResponse createClientSpecifiedDueDateCharge(double amount) {
        return createCharge(ChargeRequestBuilders.clientSpecifiedDueDateFee(amount));
    }

    public PostClientsClientIdChargesResponse addClientCharge(Long clientId, PostClientsClientIdChargesRequest request) {
        return ok(() -> corevanceClient.clientCharges().createClientCharge(clientId, request));
    }

    public PostClientsClientIdChargesChargeIdResponse payClientCharge(Long clientId, Long chargeId,
            PostClientsClientIdChargesChargeIdRequest request) {
        return ok(() -> corevanceClient.clientCharges().payOrWaiveClientCharge(clientId, chargeId, request, PAY_COMMAND));
    }

    public GetClientsClientIdChargesResponse getClientCharges(Long clientId) {
        return ok(() -> corevanceClient.clientCharges().retrieveAllClientCharges(clientId, null, null, null, null));
    }

    public DeleteClientsClientIdChargesChargeIdResponse deleteClientCharge(Long clientId, Long chargeId) {
        return ok(() -> corevanceClient.clientCharges().deleteClientCharge(clientId, chargeId));
    }
}
