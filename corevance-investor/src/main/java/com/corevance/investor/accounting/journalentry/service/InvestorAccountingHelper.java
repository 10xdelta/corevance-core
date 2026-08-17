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
package com.corevance.investor.accounting.journalentry.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import com.corevance.accounting.closure.domain.GLClosure;
import com.corevance.accounting.closure.domain.GLClosureRepository;
import com.corevance.accounting.common.AccountingConstants;
import com.corevance.accounting.common.AccountingConstants.FinancialActivity;
import com.corevance.accounting.financialactivityaccount.domain.FinancialActivityAccount;
import com.corevance.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import com.corevance.accounting.glaccount.domain.GLAccount;
import com.corevance.accounting.journalentry.domain.JournalEntry;
import com.corevance.accounting.journalentry.domain.JournalEntryRepository;
import com.corevance.accounting.journalentry.domain.JournalEntryType;
import com.corevance.accounting.journalentry.exception.JournalEntryInvalidException;
import com.corevance.accounting.journalentry.exception.JournalEntryInvalidException.GlJournalEntryInvalidReason;
import com.corevance.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import com.corevance.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import com.corevance.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingNotFoundException;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.organisation.office.domain.Office;
import com.corevance.portfolio.PortfolioProductType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InvestorAccountingHelper {

    public static final String INVESTOR_TRANSFER_IDENTIFIER = "I";

    private final JournalEntryRepository glJournalEntryRepository;
    private final ProductToGLAccountMappingRepository accountMappingRepository;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepository;
    private final GLClosureRepository closureRepository;

    /**
     * @param officeId
     * @param transactionDate
     */
    public void checkForBranchClosures(Long officeId, final LocalDate transactionDate) {
        /**
         * check if an accounting closure has happened for this branch after the transaction Date
         **/
        GLClosure gLClosure = getLatestClosureByBranch(officeId);
        if (gLClosure != null && !DateUtils.isAfter(transactionDate, gLClosure.getClosingDate())) {
            throw new JournalEntryInvalidException(GlJournalEntryInvalidReason.ACCOUNTING_CLOSED, gLClosure.getClosingDate(), null, null);
        }
    }

    public JournalEntry createDebitJournalEntryOrReversalForInvestor(final Office office, final String currencyCode,
            final int accountMappingTypeId, final Long loanProductId, final Long loanId, final Long transactionId,
            final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversalOrder) {
        final GLAccount account = getLinkedGLAccountForLoanProduct(loanProductId, accountMappingTypeId);
        if (isReversalOrder) {
            return createCreditJournalEntryForInvestor(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        } else {
            return createDebitJournalEntryForInvestor(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        }
    }

    public JournalEntry createCreditJournalEntryOrReversalForInvestor(final Office office, final String currencyCode, final Long loanId,
            final Long transactionId, final LocalDate transactionDate, final BigDecimal amount, final Boolean isReversalOrder,
            final GLAccount account) {
        if (isReversalOrder) {
            return createDebitJournalEntryForInvestor(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        } else {
            return createCreditJournalEntryForInvestor(office, currencyCode, account, loanId, transactionId, transactionDate, amount);
        }
    }

    public ProductToGLAccountMapping getChargeOffMappingByCodeValue(final Long loanProductId, final PortfolioProductType productType,
            final Long chargeOffReasonId) {
        return accountMappingRepository.findChargeOffReasonMapping(loanProductId, productType.getValue(), chargeOffReasonId);
    }

    private JournalEntry createCreditJournalEntryForInvestor(final Office office, final String currencyCode, final GLAccount account,
            final Long loanId, final Long transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        final String modifiedTransactionId = INVESTOR_TRANSFER_IDENTIFIER + transactionId;
        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.CREDIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId, null, null, null,
                null, null);
        return this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    private JournalEntry createDebitJournalEntryForInvestor(final Office office, final String currencyCode, final GLAccount account,
            final Long loanId, final Long transactionId, final LocalDate transactionDate, final BigDecimal amount) {
        final boolean manualEntry = false;
        String modifiedTransactionId = INVESTOR_TRANSFER_IDENTIFIER + transactionId;

        final JournalEntry journalEntry = JournalEntry.createNew(office, null, account, currencyCode, modifiedTransactionId, manualEntry,
                transactionDate, JournalEntryType.DEBIT, amount, null, PortfolioProductType.LOAN.getValue(), loanId, null, null, null, null,
                null);
        return this.glJournalEntryRepository.saveAndFlush(journalEntry);
    }

    public GLAccount getLinkedGLAccountForLoanProduct(final Long loanProductId, final int accountMappingTypeId) {
        GLAccount glAccount;
        if (isOrganizationAccount(accountMappingTypeId)) {
            FinancialActivityAccount financialActivityAccount = this.financialActivityAccountRepository
                    .findByFinancialActivityTypeWithNotFoundDetection(accountMappingTypeId);
            glAccount = financialActivityAccount.getGlAccount();
        } else {
            ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(loanProductId,
                    PortfolioProductType.LOAN.getValue(), accountMappingTypeId);

            if (accountMapping == null) {
                throw new ProductToGLAccountMappingNotFoundException(PortfolioProductType.LOAN, loanProductId,
                        AccountingConstants.AccrualAccountsForLoan.fromInt(accountMappingTypeId).toString());
            }
            glAccount = accountMapping.getGlAccount();
        }
        return glAccount;
    }

    private boolean isOrganizationAccount(final int accountMappingTypeId) {
        return FinancialActivity.fromInt(accountMappingTypeId) != null;
    }

    public GLClosure getLatestClosureByBranch(final long officeId) {
        return this.closureRepository.getLatestGLClosureByBranch(officeId);
    }

}
