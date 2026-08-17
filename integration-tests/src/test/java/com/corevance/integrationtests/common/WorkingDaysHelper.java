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
package com.corevance.integrationtests.common;

import static com.corevance.client.feign.util.FeignCalls.fail;
import static com.corevance.client.feign.util.FeignCalls.ok;

import java.security.SecureRandom;
import com.corevance.client.feign.util.CallFailedRuntimeException;
import com.corevance.client.models.WorkingDaysData;
import com.corevance.client.models.WorkingDaysUpdateRequest;
import com.corevance.client.models.WorkingDaysUpdateResponse;

public final class WorkingDaysHelper {

    private WorkingDaysHelper() {

    }

    private static final SecureRandom random = new SecureRandom();

    public static WorkingDaysUpdateResponse updateWorkingDays() {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingDays().updateWorkingDay(getUpdateWorkingDaysRequest()));
    }

    public static CallFailedRuntimeException updateWorkingDaysWithWrongRecurrence() {
        return fail(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingDays()
                .updateWorkingDay(getUpdateWorkingDaysWithWrongRecurrenceRequest()));
    }

    public static WorkingDaysUpdateResponse updateWorkingDaysWeekDays() {
        return ok(
                () -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingDays().updateWorkingDay(getUpdateWorkingWeekDaysRequest()));
    }

    public static WorkingDaysUpdateRequest getUpdateWorkingWeekDaysRequest() {
        return new WorkingDaysUpdateRequest().recurrence("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR").repaymentRescheduleType(2)
                .extendTermForDailyRepayments(false);
    }

    public static WorkingDaysUpdateRequest getUpdateWorkingDaysRequest() {
        return new WorkingDaysUpdateRequest().recurrence("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR,SA,SU")
                .repaymentRescheduleType(random.nextInt(4) + 1).extendTermForDailyRepayments(false);
    }

    public static WorkingDaysUpdateRequest getUpdateWorkingDaysWithWrongRecurrenceRequest() {
        return new WorkingDaysUpdateRequest().recurrence("FREQ=WEEKLY;INTERVAL=1;BYDAY=MP,TI,TE,TH")
                .repaymentRescheduleType(random.nextInt(4) + 1).extendTermForDailyRepayments(false);
    }

    public static int workingDaysId() {
        return Math.toIntExact(getAllWorkingDays().getId());
    }

    public static WorkingDaysData getAllWorkingDays() {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingDays().retrieveAllWorkingDays());
    }

}
