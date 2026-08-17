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
package com.corevance.integrationtests.common.loans;

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.models.COBPartition;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;

@Slf4j
public final class CobHelper {

    private CobHelper() {}

    public static List<COBPartition> getCobPartitions(int partitionSize) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().internalCob().getCobPartitions(partitionSize));

    }

    public static void fastForwardLoansLastCOBDate(final Long loanId, final String cobDate) {
        ok(() -> {
            CorevanceFeignClientHelper.getCorevanceFeignClient().internalCob().updateLoanCobLastDate(loanId,
                    "{\"lastClosedBusinessDate\":\"" + cobDate + "\"}");
            return null;
        });
    }

    public static void reprocessLoan(final Long loanId) {
        ok(() -> {
            CorevanceFeignClientHelper.getCorevanceFeignClient().internalCob().loanReprocess(loanId);
            return null;
        });
    }
}
