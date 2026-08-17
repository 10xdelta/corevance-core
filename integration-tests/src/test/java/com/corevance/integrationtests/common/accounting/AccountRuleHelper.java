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
package com.corevance.integrationtests.common.accounting;

import static com.corevance.client.feign.util.FeignCalls.ok;

import java.util.List;
import com.corevance.client.models.AccountRuleRequest;
import com.corevance.client.models.AccountingRuleData;
import com.corevance.client.models.PostAccountingRulesResponse;
import com.corevance.integrationtests.common.CorevanceFeignClientHelper;
import com.corevance.integrationtests.common.Utils;

public final class AccountRuleHelper {

    private AccountRuleHelper() {}

    public static List<AccountingRuleData> getAccountingRules() {
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().accountingRules().retrieveAllAccountingRules());
    }

    public static PostAccountingRulesResponse createAccountRule(final Long officeId, final Account accountToCredit,
            final Account accountToDebit) {
        final String name = Utils.uniqueRandomStringGenerator("ACCOUNTRULE_NAME_", 5);
        final AccountRuleRequest request = new AccountRuleRequest().officeId(officeId).name(name).description(name)
                .accountToCredit(accountToCredit.getAccountID().longValue()).accountToDebit(accountToDebit.getAccountID().longValue());
        return ok(() -> CorevanceFeignClientHelper.getCorevanceFeignClient().accountingRules().createAccountingRule(request));
    }
}
