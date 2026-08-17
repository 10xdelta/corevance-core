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
package com.corevance.integrationtests.common.workingcapitalloan;

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import com.corevance.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import com.corevance.client.models.PostWorkingCapitalLoansDelinquencyActionResponse;
import com.corevance.client.models.WorkingCapitalLoanDelinquencyActionData;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;

@Slf4j
public final class WorkingCapitalLoanDelinquencyActionHelper {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private WorkingCapitalLoanDelinquencyActionHelper() {}

    public static PostWorkingCapitalLoansDelinquencyActionResponse createDelinquencyAction(final Long loanId, final String action,
            final LocalDate startDate, final LocalDate endDate) {
        final PostWorkingCapitalLoansDelinquencyActionRequest request = buildActionRequest(action, startDate, endDate);
        log.info("Creating delinquency action for loan {} request={}", loanId, request);
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanDelinquencyActions()
                .createDelinquencyAction(loanId, request));
    }

    public static PostWorkingCapitalLoansDelinquencyActionResponse createDelinquencyAction(final Long loanId,
            final PostWorkingCapitalLoansDelinquencyActionRequest request) {
        log.info("Creating delinquency action for loan {} request={}", loanId, request);
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanDelinquencyActions()
                .createDelinquencyAction(loanId, request));
    }

    public static List<WorkingCapitalLoanDelinquencyActionData> retrieveDelinquencyActions(final Long loanId) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanDelinquencyActions()
                .retrieveDelinquencyActions(loanId));
    }

    public static PostWorkingCapitalLoansDelinquencyActionResponse disableDelinquency(final Long loanId) {
        log.info("Disabling delinquency evaluation for loan {}", loanId);
        return createDelinquencyAction(loanId, "disable", LocalDate.now(ZoneId.systemDefault()), null);
    }

    public static PostWorkingCapitalLoansDelinquencyActionResponse enableDelinquency(final Long loanId) {
        log.info("Enabling delinquency evaluation for loan {}", loanId);
        return createDelinquencyAction(loanId, "enable", LocalDate.now(ZoneId.systemDefault()), null);
    }

    public static PostWorkingCapitalLoansDelinquencyActionResponse disableDelinquencyByExternalId(final String loanExternalId) {
        log.info("Disabling delinquency evaluation for loan externalId={}", loanExternalId);
        return createDelinquencyActionByExternalId(loanExternalId, "disable", LocalDate.now(ZoneId.systemDefault()), null);
    }

    public static PostWorkingCapitalLoansDelinquencyActionResponse enableDelinquencyByExternalId(final String loanExternalId) {
        log.info("Enabling delinquency evaluation for loan externalId={}", loanExternalId);
        return createDelinquencyActionByExternalId(loanExternalId, "enable", LocalDate.now(ZoneId.systemDefault()), null);
    }

    public static PostWorkingCapitalLoansDelinquencyActionResponse createDelinquencyActionByExternalId(final String loanExternalId,
            final String action, final LocalDate startDate, final LocalDate endDate) {
        final PostWorkingCapitalLoansDelinquencyActionRequest request = buildActionRequest(action, startDate, endDate);
        log.info("Creating delinquency action for loan externalId={} request={}", loanExternalId, request);
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanDelinquencyActions()
                .createDelinquencyActionByExternalId(loanExternalId, request));
    }

    public static List<WorkingCapitalLoanDelinquencyActionData> retrieveDelinquencyActionsByExternalId(final String loanExternalId) {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().workingCapitalLoanDelinquencyActions()
                .retrieveDelinquencyActionsByExternalId(loanExternalId));
    }

    public static void activateLoan(final Long loanId, final LocalDate disbursementDate) {
        final String dateStr = disbursementDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        log.info("Activating WC loan {} with disbursement date {}", loanId, dateStr);
        ok(() -> {
            CorevanceFeignClientHelper.getCorevanceFeignClient().internalWorkingCapitalLoans().activateLoan(loanId, dateStr);
            return null;
        });
    }

    public static void generateNextDelinquencyPeriod(final Long loanId, final LocalDate businessDate) {
        final String dateStr = businessDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        log.info("Generating next delinquency period for WC loan {} with business date {}", loanId, dateStr);
        ok(() -> {
            CorevanceFeignClientHelper.getCorevanceFeignClient().internalWorkingCapitalLoans().generateNextDelinquencyPeriod(loanId, dateStr);
            return null;
        });
    }

    public static PostWorkingCapitalLoansDelinquencyActionRequest buildActionRequest(final String action, final LocalDate startDate,
            final LocalDate endDate) {
        final PostWorkingCapitalLoansDelinquencyActionRequest request = new PostWorkingCapitalLoansDelinquencyActionRequest();
        request.setAction(action);
        request.setStartDate(startDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        if (endDate != null) {
            request.setEndDate(endDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        }
        request.setDateFormat(DATE_FORMAT);
        request.setLocale("en");
        return request;
    }
}
