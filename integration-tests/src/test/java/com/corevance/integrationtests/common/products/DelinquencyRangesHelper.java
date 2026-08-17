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
package com.corevance.integrationtests.common.products;

import java.util.List;
import com.corevance.client.models.DeleteDelinquencyRangeResponse;
import com.corevance.client.models.DelinquencyRangeRequest;
import com.corevance.client.models.DelinquencyRangeResponse;
import com.corevance.client.models.PostDelinquencyRangeResponse;
import com.corevance.client.models.PutDelinquencyRangeResponse;
import com.corevance.client.util.Calls;
import com.corevance.integrationtests.common.CorevanceClientHelper;

public class DelinquencyRangesHelper {

    protected DelinquencyRangesHelper() {}

    public static List<DelinquencyRangeResponse> getRanges() {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().delinquencyRangeAndBucketsManagement.getRanges());
    }

    public static DelinquencyRangeResponse getRange(Long id) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().delinquencyRangeAndBucketsManagement.getRange(id));
    }

    public static PostDelinquencyRangeResponse createRange(DelinquencyRangeRequest delinquencyRangeData) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().delinquencyRangeAndBucketsManagement.createRange(delinquencyRangeData));
    }

    public static PutDelinquencyRangeResponse updateRange(Long id, DelinquencyRangeRequest delinquencyRangeData) {
        return Calls
                .ok(CorevanceClientHelper.getCorevanceClient().delinquencyRangeAndBucketsManagement.updateRange(id, delinquencyRangeData));
    }

    public static DeleteDelinquencyRangeResponse deleteRange(Long id) {
        return Calls.ok(CorevanceClientHelper.getCorevanceClient().delinquencyRangeAndBucketsManagement.deleteRange(id));
    }
}
