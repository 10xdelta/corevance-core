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
package com.corevance.client.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import com.corevance.client.auth.ApiKeyAuth;
import com.corevance.client.auth.HttpBasicAuth;
import com.corevance.client.services.AccountNumberFormatApi;
import com.corevance.client.services.AccountTransfersApi;
import com.corevance.client.services.AccountingClosureApi;
import com.corevance.client.services.AccountingRulesApi;
import com.corevance.client.services.AdhocQueryApiApi;
import com.corevance.client.services.AuditsApi;
import com.corevance.client.services.AuthenticationHttpBasicApi;
import com.corevance.client.services.BatchApiApi;
import com.corevance.client.services.BusinessDateManagementApi;
import com.corevance.client.services.BusinessStepConfigurationApi;
import com.corevance.client.services.CacheApi;
import com.corevance.client.services.CashierJournalsApi;
import com.corevance.client.services.CashiersApi;
import com.corevance.client.services.CentersApi;
import com.corevance.client.services.ChargesApi;
import com.corevance.client.services.ClientApi;
import com.corevance.client.services.ClientChargesApi;
import com.corevance.client.services.ClientIdentifierApi;
import com.corevance.client.services.ClientSearchV2Api;
import com.corevance.client.services.ClientTransactionApi;
import com.corevance.client.services.ClientsAddressApi;
import com.corevance.client.services.CodeValuesApi;
import com.corevance.client.services.CodesApi;
import com.corevance.client.services.CreditBureauConfigurationApi;
import com.corevance.client.services.CreditBureauIntegrationApi;
import com.corevance.client.services.CurrencyApi;
import com.corevance.client.services.DataTablesApi;
import com.corevance.client.services.DefaultApi;
import com.corevance.client.services.DelinquencyRangeAndBucketsManagementApi;
import com.corevance.client.services.DocumentsApiFixed;
import com.corevance.client.services.EntityDataTableApi;
import com.corevance.client.services.EntityFieldConfigurationApi;
import com.corevance.client.services.ExternalAssetOwnerLoanProductAttributesApi;
import com.corevance.client.services.ExternalAssetOwnersApi;
import com.corevance.client.services.ExternalEventConfigurationApi;
import com.corevance.client.services.ExternalServicesApi;
import com.corevance.client.services.FetchAuthenticatedUserDetailsApi;
import com.corevance.client.services.FixedDepositAccountApi;
import com.corevance.client.services.FixedDepositAccountTransactionsApi;
import com.corevance.client.services.FixedDepositProductApi;
import com.corevance.client.services.FloatingRatesApi;
import com.corevance.client.services.GeneralLedgerAccountApi;
import com.corevance.client.services.GlobalConfigurationApi;
import com.corevance.client.services.GroupsApi;
import com.corevance.client.services.HolidaysApi;
import com.corevance.client.services.HooksApi;
import com.corevance.client.services.ImagesApi;
import com.corevance.client.services.InlineJobApi;
import com.corevance.client.services.InterestRateChartApi;
import com.corevance.client.services.InterestRateSlabAKAInterestBandsApi;
import com.corevance.client.services.InternalCobApi;
import com.corevance.client.services.InternalWorkingCapitalLoansApi;
import com.corevance.client.services.JournalEntriesApi;
import com.corevance.client.services.ListReportMailingJobHistoryApi;
import com.corevance.client.services.LoanAccountLockApi;
import com.corevance.client.services.LoanBuyDownFeesApi;
import com.corevance.client.services.LoanCapitalizedIncomeApi;
import com.corevance.client.services.LoanChargesApi;
import com.corevance.client.services.LoanCobCatchUpApi;
import com.corevance.client.services.LoanCollateralApi;
import com.corevance.client.services.LoanDisbursementDetailsApi;
import com.corevance.client.services.LoanInterestPauseApi;
import com.corevance.client.services.LoanProductsApi;
import com.corevance.client.services.LoanProductsDetailsApi;
import com.corevance.client.services.LoanReschedulingApi;
import com.corevance.client.services.LoanTransactionsApi;
import com.corevance.client.services.LoansApi;
import com.corevance.client.services.LoansPointInTimeApi;
import com.corevance.client.services.MakerCheckerOr4EyeFunctionalityApi;
import com.corevance.client.services.MappingFinancialActivitiesToAccountsApi;
import com.corevance.client.services.MixMappingApi;
import com.corevance.client.services.MixReportApi;
import com.corevance.client.services.MixTaxonomyApi;
import com.corevance.client.services.NotesApi;
import com.corevance.client.services.NotificationApi;
import com.corevance.client.services.OfficesApi;
import com.corevance.client.services.PasswordPreferencesApi;
import com.corevance.client.services.PaymentTypeApi;
import com.corevance.client.services.PeriodicAccrualAccountingApi;
import com.corevance.client.services.PermissionsApi;
import com.corevance.client.services.ProductsApi;
import com.corevance.client.services.ProgressiveLoanApi;
import com.corevance.client.services.ProvisioningCategoryApi;
import com.corevance.client.services.ProvisioningCriteriaApi;
import com.corevance.client.services.ProvisioningEntriesApi;
import com.corevance.client.services.RecurringDepositAccountApi;
import com.corevance.client.services.RecurringDepositAccountTransactionsApi;
import com.corevance.client.services.RecurringDepositProductApi;
import com.corevance.client.services.ReportMailingJobsApi;
import com.corevance.client.services.ReportsApi;
import com.corevance.client.services.RescheduleLoansApi;
import com.corevance.client.services.RolesApi;
import com.corevance.client.services.RunReportsApi;
import com.corevance.client.services.SavingsAccountApi;
import com.corevance.client.services.SavingsAccountTransactionsApi;
import com.corevance.client.services.SavingsChargesApi;
import com.corevance.client.services.SavingsProductApi;
import com.corevance.client.services.SchedulerApi;
import com.corevance.client.services.SchedulerJobApi;
import com.corevance.client.services.ScoreCardApi;
import com.corevance.client.services.SearchApiApi;
import com.corevance.client.services.ShareAccountApi;
import com.corevance.client.services.SpmApiLookUpTableApi;
import com.corevance.client.services.SpmSurveysApi;
import com.corevance.client.services.StaffApi;
import com.corevance.client.services.StandingInstructionsApi;
import com.corevance.client.services.StandingInstructionsHistoryApi;
import com.corevance.client.services.TaxComponentsApi;
import com.corevance.client.services.TaxGroupApi;
import com.corevance.client.services.TellerCashManagementApi;
import com.corevance.client.services.TemplatesApi;
import com.corevance.client.services.UsersApi;
import com.corevance.client.services.WorkingCapitalBreachApi;
import com.corevance.client.services.WorkingCapitalLoanAccountLockApi;
import com.corevance.client.services.WorkingCapitalLoanBreachActionsApi;
import com.corevance.client.services.WorkingCapitalLoanBreachScheduleApi;
import com.corevance.client.services.WorkingCapitalLoanChargesApi;
import com.corevance.client.services.WorkingCapitalLoanCobCatchUpApi;
import com.corevance.client.services.WorkingCapitalLoanDelinquencyActionsApi;
import com.corevance.client.services.WorkingCapitalLoanDelinquencyRangeScheduleApi;
import com.corevance.client.services.WorkingCapitalLoanInternalCobApiApi;
import com.corevance.client.services.WorkingCapitalLoanNearBreachActionsApi;
import com.corevance.client.services.WorkingCapitalLoanOriginatorsApi;
import com.corevance.client.services.WorkingCapitalLoanProductsApi;
import com.corevance.client.services.WorkingCapitalLoanTransactionsApi;
import com.corevance.client.services.WorkingCapitalLoansApi;
import com.corevance.client.services.WorkingCapitalNearBreachApi;
import com.corevance.client.services.WorkingDaysApi;
import com.corevance.client.util.JSON.GsonCustomConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Corevance Client Java SDK API entry point.
 *
 * @author Michael Vorburger.ch
 */
public final class CorevanceClient {

    /**
     * Constant to be used in requests where Corevance's API requires a dateFormat to be given. This matches the format
     * in which LocalDate instances are serialized. (BTW: In a Java client API, it seems weird to have strong LocalDate
     * (not String) instances, and then have to specify its format, see
     * https://issues.apache.org/jira/browse/COREVANCE-1233.)
     */
    // Matching com.corevance.client.util.JSON.LocalDateTypeAdapter.formatter
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private final OkHttpClient okHttpClient;
    private final Retrofit retrofit;

    public final AccountingClosureApi glClosures;
    public final AccountingRulesApi accountingRules;
    public final AccountNumberFormatApi accountNumberFormats;
    public final AccountTransfersApi accountTransfers;
    public final AdhocQueryApiApi adhocQuery;
    public final AuditsApi audits;
    public final AuthenticationHttpBasicApi authentication;
    public final BatchApiApi batches;
    public final BusinessDateManagementApi businessDateManagement;
    public final BusinessStepConfigurationApi businessStepConfiguration;
    public final CacheApi caches;
    public final CashierJournalsApi cashiersJournal;
    public final CashiersApi cashiers;
    public final CentersApi centers;
    public final ChargesApi charges;
    public final ClientApi clients;
    public final CreditBureauConfigurationApi creditBureauConfiguration;
    public final CreditBureauIntegrationApi creditBureauIntegration;

    public final ClientSearchV2Api clientSearchV2;
    public final ClientChargesApi clientCharges;
    public final ClientIdentifierApi clientIdentifiers;
    public final ClientsAddressApi clientAddresses;
    public final ClientTransactionApi clientTransactions;
    public final CodesApi codes;
    public final CodeValuesApi codeValues;
    public final CurrencyApi currencies;
    public final DataTablesApi dataTables;
    public final @Deprecated DefaultApi legacy; // TODO COREVANCE-1222
    public final DocumentsApiFixed documents;
    public final DelinquencyRangeAndBucketsManagementApi delinquencyRangeAndBucketsManagement;
    public final EntityDataTableApi entityDatatableChecks;
    public final EntityFieldConfigurationApi entityFieldConfigurations;
    public final ExternalEventConfigurationApi externalEventConfigurationApi;
    public final ExternalServicesApi externalServices;
    public final FetchAuthenticatedUserDetailsApi userDetails;
    public final FixedDepositAccountApi fixedDepositAccounts;
    public final FixedDepositProductApi fixedDepositProducts;
    public final FloatingRatesApi floatingRates;
    public final GeneralLedgerAccountApi glAccounts;
    public final GlobalConfigurationApi globalConfigurations;
    public final GroupsApi groups;
    public final HolidaysApi holidays;
    public final HooksApi hooks;
    public final ImagesApi images;
    public final InternalCobApi internalCob;
    public final InterestRateChartApi interestRateCharts;
    public final InterestRateSlabAKAInterestBandsApi interestRateChartLabs;
    public final JournalEntriesApi journalEntries;
    public final ListReportMailingJobHistoryApi reportMailings;
    public final LoanChargesApi loanCharges;
    public final LoanCobCatchUpApi loanCobCatchUpApi;
    public final LoanCollateralApi loanCollaterals;
    public final LoanCapitalizedIncomeApi loanCapitalizedIncome;
    public final LoanProductsApi loanProducts;
    public final LoanProductsDetailsApi loanProductsDetails;
    public final LoanReschedulingApi loanSchedules;
    public final LoansPointInTimeApi loansPointInTimeApi;
    public final LoansApi loans;
    public final LoanDisbursementDetailsApi loanDisbursementDetails;
    public final LoanTransactionsApi loanTransactions;
    public final MakerCheckerOr4EyeFunctionalityApi makerCheckers;
    public final MappingFinancialActivitiesToAccountsApi financialActivyAccountMappings;
    public final MixMappingApi mixMappings;
    public final MixReportApi mixReports;
    public final MixTaxonomyApi mixTaxonomies;
    public final NotesApi notes;
    public final NotificationApi notifications;
    public final OfficesApi offices;
    public final PasswordPreferencesApi passwordPreferences;
    public final PaymentTypeApi paymentTypes;
    public final PeriodicAccrualAccountingApi periodicAccrualAccounting;
    public final PermissionsApi permissions;
    public final ProvisioningCategoryApi provisioningCategories;
    public final ProvisioningCriteriaApi provisioningCriterias;
    public final ProvisioningEntriesApi provisioningEntries;
    public final RecurringDepositAccountApi recurringDepositAccounts;
    public final FixedDepositAccountTransactionsApi fixedDepositAccountTransactions;
    public final RecurringDepositAccountTransactionsApi recurringDepositAccountTransactions;
    public final RecurringDepositProductApi recurringDepositProducts;
    public final ReportMailingJobsApi reportMailingJobs;
    public final ReportsApi reports;
    public final RescheduleLoansApi rescheduleLoans;
    public final RolesApi roles;
    public final RunReportsApi reportsRun;
    public final SavingsAccountApi savingsAccounts;
    public final SavingsChargesApi savingsAccountCharges;
    public final SavingsProductApi savingsProducts;
    public final SavingsAccountTransactionsApi savingsTransactions;
    public final SchedulerApi jobsScheduler;
    public final SchedulerJobApi jobs;
    public final ScoreCardApi surveyScorecards;
    public final SearchApiApi search;
    public final ProductsApi shareProducts;
    public final ShareAccountApi shareAccounts;
    public final SpmApiLookUpTableApi surveyLookupTables;
    public final SpmSurveysApi surveys;
    public final StaffApi staff;
    public final StandingInstructionsApi standingInstructions;
    public final StandingInstructionsHistoryApi standingInstructionsHistory;
    public final TaxComponentsApi taxComponents;
    public final TaxGroupApi taxGroups;
    public final TellerCashManagementApi tellers;
    public final TemplatesApi templates;
    public final UsersApi users;
    public final WorkingDaysApi workingDays;
    public final LoanInterestPauseApi loanInterestPauseApi;
    public final ProgressiveLoanApi progressiveLoanApi;

    public final ExternalAssetOwnersApi externalAssetOwners;
    public final ExternalAssetOwnerLoanProductAttributesApi externalAssetOwnerLoanProductAttributes;
    public final LoanAccountLockApi loanAccountLockApi;
    public final InlineJobApi inlineJobApi;
    public final LoanBuyDownFeesApi loanBuyDownFeesApi;

    public final WorkingCapitalLoanProductsApi workingCapitalLoanProducts;
    public final WorkingCapitalLoanAccountLockApi workingCapitalLoanAccountLock;
    public final WorkingCapitalLoanCobCatchUpApi workingCapitalLoanCobCatchUpApi;
    public final WorkingCapitalLoanDelinquencyActionsApi workingCapitalLoanDelinquencyActions;
    public final WorkingCapitalLoanDelinquencyRangeScheduleApi workingCapitalLoanDelinquencyRangeSchedule;
    public final WorkingCapitalLoanBreachScheduleApi workingCapitalLoanBreachSchedule;
    public final WorkingCapitalLoanBreachActionsApi workingCapitalLoanBreachActions;
    public final InternalWorkingCapitalLoansApi internalWorkingCapitalLoans;
    public final WorkingCapitalLoansApi workingCapitalLoans;
    public final WorkingCapitalLoanChargesApi workingCapitalLoanCharges;
    public final WorkingCapitalLoanTransactionsApi workingCapitalLoanTransactions;
    public final WorkingCapitalLoanInternalCobApiApi workingCapitalLoanInternalCobApi;
    public final WorkingCapitalBreachApi workingCapitalBreaches;
    public final WorkingCapitalNearBreachApi workingCapitalNearBreaches;
    public final WorkingCapitalLoanNearBreachActionsApi workingCapitalLoanNearBreachActions;
    public final WorkingCapitalLoanOriginatorsApi workingCapitalLoanOriginators;

    private CorevanceClient(OkHttpClient okHttpClient, Retrofit retrofit) {
        this.okHttpClient = okHttpClient;
        this.retrofit = retrofit;

        loanAccountLockApi = retrofit.create(LoanAccountLockApi.class);
        externalAssetOwners = retrofit.create(ExternalAssetOwnersApi.class);
        externalAssetOwnerLoanProductAttributes = retrofit.create(ExternalAssetOwnerLoanProductAttributesApi.class);
        glClosures = retrofit.create(AccountingClosureApi.class);
        accountingRules = retrofit.create(AccountingRulesApi.class);
        accountNumberFormats = retrofit.create(AccountNumberFormatApi.class);
        accountTransfers = retrofit.create(AccountTransfersApi.class);
        adhocQuery = retrofit.create(AdhocQueryApiApi.class);
        audits = retrofit.create(AuditsApi.class);
        authentication = retrofit.create(AuthenticationHttpBasicApi.class);
        batches = retrofit.create(BatchApiApi.class);
        businessDateManagement = retrofit.create(BusinessDateManagementApi.class);
        businessStepConfiguration = retrofit.create(BusinessStepConfigurationApi.class);
        externalEventConfigurationApi = retrofit.create(ExternalEventConfigurationApi.class);
        caches = retrofit.create(CacheApi.class);
        cashiersJournal = retrofit.create(CashierJournalsApi.class);
        cashiers = retrofit.create(CashiersApi.class);
        centers = retrofit.create(CentersApi.class);
        charges = retrofit.create(ChargesApi.class);
        clients = retrofit.create(ClientApi.class);
        creditBureauConfiguration = retrofit.create(CreditBureauConfigurationApi.class);
        creditBureauIntegration = retrofit.create(CreditBureauIntegrationApi.class);
        clientSearchV2 = retrofit.create(ClientSearchV2Api.class);
        clientCharges = retrofit.create(ClientChargesApi.class);
        clientIdentifiers = retrofit.create(ClientIdentifierApi.class);
        clientAddresses = retrofit.create(ClientsAddressApi.class);
        clientTransactions = retrofit.create(ClientTransactionApi.class);
        codes = retrofit.create(CodesApi.class);
        codeValues = retrofit.create(CodeValuesApi.class);
        currencies = retrofit.create(CurrencyApi.class);
        dataTables = retrofit.create(DataTablesApi.class);
        delinquencyRangeAndBucketsManagement = retrofit.create(DelinquencyRangeAndBucketsManagementApi.class);
        legacy = retrofit.create(DefaultApi.class);
        documents = retrofit.create(DocumentsApiFixed.class);
        entityDatatableChecks = retrofit.create(EntityDataTableApi.class);
        entityFieldConfigurations = retrofit.create(EntityFieldConfigurationApi.class);
        externalServices = retrofit.create(ExternalServicesApi.class);
        userDetails = retrofit.create(FetchAuthenticatedUserDetailsApi.class);
        fixedDepositAccounts = retrofit.create(FixedDepositAccountApi.class);
        fixedDepositProducts = retrofit.create(FixedDepositProductApi.class);
        floatingRates = retrofit.create(FloatingRatesApi.class);
        glAccounts = retrofit.create(GeneralLedgerAccountApi.class);
        globalConfigurations = retrofit.create(GlobalConfigurationApi.class);
        groups = retrofit.create(GroupsApi.class);
        holidays = retrofit.create(HolidaysApi.class);
        hooks = retrofit.create(HooksApi.class);
        images = retrofit.create(ImagesApi.class);
        internalCob = retrofit.create(InternalCobApi.class);
        interestRateCharts = retrofit.create(InterestRateChartApi.class);
        interestRateChartLabs = retrofit.create(InterestRateSlabAKAInterestBandsApi.class);
        journalEntries = retrofit.create(JournalEntriesApi.class);
        reportMailings = retrofit.create(ListReportMailingJobHistoryApi.class);
        loanCharges = retrofit.create(LoanChargesApi.class);
        loanCobCatchUpApi = retrofit.create(LoanCobCatchUpApi.class);
        loanCollaterals = retrofit.create(LoanCollateralApi.class);
        loanCapitalizedIncome = retrofit.create(LoanCapitalizedIncomeApi.class);
        loanProducts = retrofit.create(LoanProductsApi.class);
        loanProductsDetails = retrofit.create(LoanProductsDetailsApi.class);
        loanSchedules = retrofit.create(LoanReschedulingApi.class);
        loansPointInTimeApi = retrofit.create(LoansPointInTimeApi.class);
        loans = retrofit.create(LoansApi.class);
        loanDisbursementDetails = retrofit.create(LoanDisbursementDetailsApi.class);
        loanTransactions = retrofit.create(LoanTransactionsApi.class);
        makerCheckers = retrofit.create(MakerCheckerOr4EyeFunctionalityApi.class);
        financialActivyAccountMappings = retrofit.create(MappingFinancialActivitiesToAccountsApi.class);
        jobs = retrofit.create(SchedulerJobApi.class);
        mixMappings = retrofit.create(MixMappingApi.class);
        mixReports = retrofit.create(MixReportApi.class);
        mixTaxonomies = retrofit.create(MixTaxonomyApi.class);
        notes = retrofit.create(NotesApi.class);
        notifications = retrofit.create(NotificationApi.class);
        offices = retrofit.create(OfficesApi.class);
        passwordPreferences = retrofit.create(PasswordPreferencesApi.class);
        paymentTypes = retrofit.create(PaymentTypeApi.class);
        periodicAccrualAccounting = retrofit.create(PeriodicAccrualAccountingApi.class);
        permissions = retrofit.create(PermissionsApi.class);
        provisioningCategories = retrofit.create(ProvisioningCategoryApi.class);
        provisioningCriterias = retrofit.create(ProvisioningCriteriaApi.class);
        provisioningEntries = retrofit.create(ProvisioningEntriesApi.class);
        recurringDepositAccounts = retrofit.create(RecurringDepositAccountApi.class);
        fixedDepositAccountTransactions = retrofit.create(FixedDepositAccountTransactionsApi.class);
        recurringDepositAccountTransactions = retrofit.create(RecurringDepositAccountTransactionsApi.class);
        recurringDepositProducts = retrofit.create(RecurringDepositProductApi.class);
        reportMailingJobs = retrofit.create(ReportMailingJobsApi.class);
        reports = retrofit.create(ReportsApi.class);
        rescheduleLoans = retrofit.create(RescheduleLoansApi.class);
        roles = retrofit.create(RolesApi.class);
        reportsRun = retrofit.create(RunReportsApi.class);
        savingsAccounts = retrofit.create(SavingsAccountApi.class);
        savingsAccountCharges = retrofit.create(SavingsChargesApi.class);
        savingsProducts = retrofit.create(SavingsProductApi.class);
        savingsTransactions = retrofit.create(SavingsAccountTransactionsApi.class);
        jobsScheduler = retrofit.create(SchedulerApi.class);
        surveyScorecards = retrofit.create(ScoreCardApi.class);
        search = retrofit.create(SearchApiApi.class);
        shareProducts = retrofit.create(ProductsApi.class);
        shareAccounts = retrofit.create(ShareAccountApi.class);
        surveyLookupTables = retrofit.create(SpmApiLookUpTableApi.class);
        surveys = retrofit.create(SpmSurveysApi.class);
        staff = retrofit.create(StaffApi.class);
        standingInstructions = retrofit.create(StandingInstructionsApi.class);
        standingInstructionsHistory = retrofit.create(StandingInstructionsHistoryApi.class);
        taxComponents = retrofit.create(TaxComponentsApi.class);
        taxGroups = retrofit.create(TaxGroupApi.class);
        tellers = retrofit.create(TellerCashManagementApi.class);
        templates = retrofit.create(TemplatesApi.class);
        users = retrofit.create(UsersApi.class);
        workingDays = retrofit.create(WorkingDaysApi.class);
        loanInterestPauseApi = retrofit.create(LoanInterestPauseApi.class);
        progressiveLoanApi = retrofit.create(ProgressiveLoanApi.class);
        inlineJobApi = retrofit.create(InlineJobApi.class);
        loanBuyDownFeesApi = retrofit.create(LoanBuyDownFeesApi.class);
        workingCapitalLoanProducts = retrofit.create(WorkingCapitalLoanProductsApi.class);
        workingCapitalLoanAccountLock = retrofit.create(WorkingCapitalLoanAccountLockApi.class);
        workingCapitalLoanCobCatchUpApi = retrofit.create(WorkingCapitalLoanCobCatchUpApi.class);
        workingCapitalLoanDelinquencyActions = retrofit.create(WorkingCapitalLoanDelinquencyActionsApi.class);
        workingCapitalLoanDelinquencyRangeSchedule = retrofit.create(WorkingCapitalLoanDelinquencyRangeScheduleApi.class);
        workingCapitalLoanBreachSchedule = retrofit.create(WorkingCapitalLoanBreachScheduleApi.class);
        workingCapitalLoanBreachActions = retrofit.create(WorkingCapitalLoanBreachActionsApi.class);
        internalWorkingCapitalLoans = retrofit.create(InternalWorkingCapitalLoansApi.class);
        workingCapitalLoans = retrofit.create(WorkingCapitalLoansApi.class);
        workingCapitalLoanCharges = retrofit.create(WorkingCapitalLoanChargesApi.class);
        workingCapitalLoanTransactions = retrofit.create(WorkingCapitalLoanTransactionsApi.class);
        workingCapitalLoanInternalCobApi = retrofit.create(WorkingCapitalLoanInternalCobApiApi.class);
        workingCapitalBreaches = retrofit.create(WorkingCapitalBreachApi.class);
        workingCapitalNearBreaches = retrofit.create(WorkingCapitalNearBreachApi.class);
        workingCapitalLoanNearBreachActions = retrofit.create(WorkingCapitalLoanNearBreachActionsApi.class);
        workingCapitalLoanOriginators = retrofit.create(WorkingCapitalLoanOriginatorsApi.class);
    }

    public static Builder builder() {
        return new Builder();
    }

    public OkHttpClient okHttpClient() {
        return this.okHttpClient;
    }

    public HttpUrl baseURL() {
        return this.retrofit.baseUrl();
    }

    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface, using
     * {@link Retrofit#create(Class)}. This method is typically not required to be invoked for standard API usage, but
     * can be a handy back door for non-trivial advanced customizations of the API client if you have extended Corevance
     * with your own REST APIs.
     */
    public <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }

    public static final class Builder {

        private static final Logger log = LoggerFactory.getLogger(Builder.class);

        private final JSON json = new JSON();
        private final OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
        private final Retrofit.Builder retrofitBuilder = new Retrofit.Builder().addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonCustomConverterFactory.create(json.getGson()));

        private String baseURL;
        private String tenant;
        private String username;
        private String password;

        private Builder() {}

        public Builder baseURL(String baseURL) {
            this.baseURL = baseURL;
            return this;
        }

        public Builder tenant(String tenant) {
            this.tenant = tenant;
            return this;
        }

        public Builder basicAuth(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder logging(Level level) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(level);
            okBuilder.addInterceptor(logging);
            return this;
        }

        public Builder readTimeout(Duration timeout) {
            okBuilder.readTimeout(timeout);
            return this;
        }

        /**
         * Skip Corevance API host SSL certificate verification. DO NOT USE THIS when invoking a production server's API!
         * This is intended for https://localhost:8443/ testing of development servers with self-signed certificates,
         * only. If you do not understand what this is, do not use it. You WILL cause a security issue in your
         * application due to the possibility of a "man in the middle" attack when this is enabled.
         */
        @SuppressWarnings("unused")
        public Builder insecure(boolean insecure) {
            // Nota bene: Similar code to this is also in Corevance Provider's
            // com.corevance.infrastructure.hooks.processor.ProcessorHelper
            if (insecure) {
                HostnameVerifier insecureHostnameVerifier = (hostname, session) -> true;// NOSONAR
                okBuilder.hostnameVerifier(insecureHostnameVerifier);

                try {
                    X509TrustManager insecureX509TrustManager = new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}// NOSONAR

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}// NOSONAR

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[] {};
                        }
                    };

                    // TODO "SSL" or "TLS" as in hooks.processor.ProcessorHelper?
                    SSLContext sslContext = SSLContext.getInstance("SSL");// NOSONAR
                    sslContext.init(null, new TrustManager[] { insecureX509TrustManager }, new SecureRandom());
                    SSLSocketFactory insecureSslSocketFactory = sslContext.getSocketFactory();

                    okBuilder.sslSocketFactory(insecureSslSocketFactory, insecureX509TrustManager);
                } catch (NoSuchAlgorithmException | KeyManagementException e) {
                    throw new IllegalStateException("insecure() SSL configuration failed", e);
                }
            }
            return this;
        }

        public CorevanceClient build() {
            // URL
            retrofitBuilder.baseUrl(has("baseURL", baseURL));

            // Tenant
            if (tenant != null) {
                ApiKeyAuth tenantAuth = new ApiKeyAuth("header", "corevance-platform-tenantid");
                tenantAuth.setApiKey(has("tenant", tenant));
                okBuilder.addInterceptor(tenantAuth);
            } else {
                log.warn("Tenant hasn't been configured for the client");
            }

            // BASIC Auth
            if (username != null && password != null) {
                HttpBasicAuth basicAuth = new HttpBasicAuth();
                basicAuth.setCredentials(has("username", username), has("password", password));
                okBuilder.addInterceptor(basicAuth);
            } else {
                log.warn("Username and password haven't been configured for the client");
            }

            OkHttpClient okHttpClient = okBuilder.build();
            retrofitBuilder.client(okHttpClient);

            return new CorevanceClient(okHttpClient, retrofitBuilder.build());
        }

        /**
         * Obtain the internal Retrofit Builder. This method is typically not required to be invoked for simple API
         * usages, but can be a handy back door for non-trivial advanced customizations of the API client.
         *
         */
        public retrofit2.Retrofit.Builder getRetrofitBuilder() {
            return retrofitBuilder;
        }

        /**
         * Obtain the internal OkHttp Builder. This method is typically not required to be invoked for simple API
         * usages, but can be a handy back door for non-trivial advanced customizations of the API client.
         *
         */
        public okhttp3.OkHttpClient.Builder getOkBuilder() {
            return okBuilder;
        }

        private <T> T has(String propertyName, T value) throws IllegalStateException {
            if (value == null) {
                throw new IllegalStateException("Must call " + propertyName + "(...) to create valid Builder");
            }
            return value;
        }
    }
}
