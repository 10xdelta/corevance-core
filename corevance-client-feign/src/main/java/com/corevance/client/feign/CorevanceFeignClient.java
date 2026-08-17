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
package com.corevance.client.feign;

import com.corevance.client.feign.services.AccountNumberFormatApi;
import com.corevance.client.feign.services.AccountTransfersApi;
import com.corevance.client.feign.services.AccountingClosureApi;
import com.corevance.client.feign.services.AccountingRulesApi;
import com.corevance.client.feign.services.AdhocQueryApiApi;
import com.corevance.client.feign.services.AuditsApi;
import com.corevance.client.feign.services.AuthenticationHttpBasicApi;
import com.corevance.client.feign.services.BatchApiApi;
import com.corevance.client.feign.services.BulkImportApi;
import com.corevance.client.feign.services.BulkLoansApi;
import com.corevance.client.feign.services.BusinessDateManagementApi;
import com.corevance.client.feign.services.BusinessStepConfigurationApi;
import com.corevance.client.feign.services.CacheApi;
import com.corevance.client.feign.services.CalendarApi;
import com.corevance.client.feign.services.CashierJournalsApi;
import com.corevance.client.feign.services.CashiersApi;
import com.corevance.client.feign.services.CentersApi;
import com.corevance.client.feign.services.ChargesApi;
import com.corevance.client.feign.services.ClientApi;
import com.corevance.client.feign.services.ClientChargesApi;
import com.corevance.client.feign.services.ClientCollateralManagementApi;
import com.corevance.client.feign.services.ClientFamilyMemberApi;
import com.corevance.client.feign.services.ClientIdentifierApi;
import com.corevance.client.feign.services.ClientSearchV2Api;
import com.corevance.client.feign.services.ClientTransactionApi;
import com.corevance.client.feign.services.ClientsAddressApi;
import com.corevance.client.feign.services.CodeValuesApi;
import com.corevance.client.feign.services.CodesApi;
import com.corevance.client.feign.services.CollateralManagementApi;
import com.corevance.client.feign.services.CollectionSheetApi;
import com.corevance.client.feign.services.CreditBureauConfigurationApi;
import com.corevance.client.feign.services.CurrencyApi;
import com.corevance.client.feign.services.DataTablesApi;
import com.corevance.client.feign.services.DefaultApi;
import com.corevance.client.feign.services.DelinquencyRangeAndBucketsManagementApi;
import com.corevance.client.feign.services.DepositAccountOnHoldFundTransactionsApi;
import com.corevance.client.feign.services.DocumentsApi;
import com.corevance.client.feign.services.DocumentsApiFixed;
import com.corevance.client.feign.services.EntityDataTableApi;
import com.corevance.client.feign.services.EntityFieldConfigurationApi;
import com.corevance.client.feign.services.ExternalAssetOwnerLoanProductAttributesApi;
import com.corevance.client.feign.services.ExternalAssetOwnersApi;
import com.corevance.client.feign.services.ExternalEventConfigurationApi;
import com.corevance.client.feign.services.ExternalServicesApi;
import com.corevance.client.feign.services.FetchAuthenticatedUserDetailsApi;
import com.corevance.client.feign.services.CorevanceEntityApi;
import com.corevance.client.feign.services.FixedDepositAccountApi;
import com.corevance.client.feign.services.FixedDepositAccountTransactionsApi;
import com.corevance.client.feign.services.FixedDepositProductApi;
import com.corevance.client.feign.services.FloatingRatesApi;
import com.corevance.client.feign.services.FundsApi;
import com.corevance.client.feign.services.GeneralLedgerAccountApi;
import com.corevance.client.feign.services.GlobalConfigurationApi;
import com.corevance.client.feign.services.GroupsApi;
import com.corevance.client.feign.services.GroupsLevelApi;
import com.corevance.client.feign.services.GuarantorsApi;
import com.corevance.client.feign.services.HolidaysApi;
import com.corevance.client.feign.services.HooksApi;
import com.corevance.client.feign.services.ImagesApi;
import com.corevance.client.feign.services.InlineJobApi;
import com.corevance.client.feign.services.InstanceModeApi;
import com.corevance.client.feign.services.InterOperationApi;
import com.corevance.client.feign.services.InterestRateChartApi;
import com.corevance.client.feign.services.InterestRateSlabAKAInterestBandsApi;
import com.corevance.client.feign.services.InternalCobApi;
import com.corevance.client.feign.services.InternalWorkingCapitalLoansApi;
import com.corevance.client.feign.services.JournalEntriesApi;
import com.corevance.client.feign.services.LikelihoodApi;
import com.corevance.client.feign.services.ListReportMailingJobHistoryApi;
import com.corevance.client.feign.services.LoanAccountLockApi;
import com.corevance.client.feign.services.LoanBuyDownFeesApi;
import com.corevance.client.feign.services.LoanCapitalizedIncomeApi;
import com.corevance.client.feign.services.LoanChargesApi;
import com.corevance.client.feign.services.LoanCobCatchUpApi;
import com.corevance.client.feign.services.LoanCollateralApi;
import com.corevance.client.feign.services.LoanCollateralManagementApi;
import com.corevance.client.feign.services.LoanDisbursementDetailsApi;
import com.corevance.client.feign.services.LoanInterestPauseApi;
import com.corevance.client.feign.services.LoanOriginatorsApi;
import com.corevance.client.feign.services.LoanProductsApi;
import com.corevance.client.feign.services.LoanReschedulingApi;
import com.corevance.client.feign.services.LoanTransactionsApi;
import com.corevance.client.feign.services.LoansApi;
import com.corevance.client.feign.services.LoansPointInTimeApi;
import com.corevance.client.feign.services.MakerCheckerOr4EyeFunctionalityApi;
import com.corevance.client.feign.services.MappingFinancialActivitiesToAccountsApi;
import com.corevance.client.feign.services.MeetingsApi;
import com.corevance.client.feign.services.MixMappingApi;
import com.corevance.client.feign.services.MixReportApi;
import com.corevance.client.feign.services.MixTaxonomyApi;
import com.corevance.client.feign.services.NotesApi;
import com.corevance.client.feign.services.NotificationApi;
import com.corevance.client.feign.services.OfficesApi;
import com.corevance.client.feign.services.PasswordPreferencesApi;
import com.corevance.client.feign.services.PaymentTypeApi;
import com.corevance.client.feign.services.PeriodicAccrualAccountingApi;
import com.corevance.client.feign.services.PermissionsApi;
import com.corevance.client.feign.services.PovertyLineApi;
import com.corevance.client.feign.services.ProductMixApi;
import com.corevance.client.feign.services.ProductsApi;
import com.corevance.client.feign.services.ProgressiveLoanApi;
import com.corevance.client.feign.services.ProvisioningCategoryApi;
import com.corevance.client.feign.services.ProvisioningCriteriaApi;
import com.corevance.client.feign.services.ProvisioningEntriesApi;
import com.corevance.client.feign.services.RateApi;
import com.corevance.client.feign.services.RecurringDepositAccountApi;
import com.corevance.client.feign.services.RecurringDepositAccountTransactionsApi;
import com.corevance.client.feign.services.RecurringDepositProductApi;
import com.corevance.client.feign.services.RepaymentWithPostDatedChecksApi;
import com.corevance.client.feign.services.ReportMailingJobsApi;
import com.corevance.client.feign.services.ReportsApi;
import com.corevance.client.feign.services.RescheduleLoansApi;
import com.corevance.client.feign.services.RolesApi;
import com.corevance.client.feign.services.RunReportsApi;
import com.corevance.client.feign.services.SavingsAccountApi;
import com.corevance.client.feign.services.SavingsAccountTransactionsApi;
import com.corevance.client.feign.services.SavingsChargesApi;
import com.corevance.client.feign.services.SavingsProductApi;
import com.corevance.client.feign.services.SchedulerApi;
import com.corevance.client.feign.services.SchedulerJobApi;
import com.corevance.client.feign.services.ScoreCardApi;
import com.corevance.client.feign.services.SearchApiApi;
import com.corevance.client.feign.services.ShareAccountApi;
import com.corevance.client.feign.services.SmsApi;
import com.corevance.client.feign.services.SpmApiLookUpTableApi;
import com.corevance.client.feign.services.SpmSurveysApi;
import com.corevance.client.feign.services.StaffApi;
import com.corevance.client.feign.services.StandingInstructionsApi;
import com.corevance.client.feign.services.StandingInstructionsHistoryApi;
import com.corevance.client.feign.services.SurveyApi;
import com.corevance.client.feign.services.TaxComponentsApi;
import com.corevance.client.feign.services.TaxGroupApi;
import com.corevance.client.feign.services.TellerCashManagementApi;
import com.corevance.client.feign.services.TemplatesApi;
import com.corevance.client.feign.services.TwoFactorApi;
import com.corevance.client.feign.services.UsersApi;
import com.corevance.client.feign.services.WorkingCapitalBreachApi;
import com.corevance.client.feign.services.WorkingCapitalLoanAccountLockApi;
import com.corevance.client.feign.services.WorkingCapitalLoanBreachActionsApi;
import com.corevance.client.feign.services.WorkingCapitalLoanBreachScheduleApi;
import com.corevance.client.feign.services.WorkingCapitalLoanChargesApi;
import com.corevance.client.feign.services.WorkingCapitalLoanCobCatchUpApi;
import com.corevance.client.feign.services.WorkingCapitalLoanDelinquencyActionsApi;
import com.corevance.client.feign.services.WorkingCapitalLoanDelinquencyRangeScheduleApi;
import com.corevance.client.feign.services.WorkingCapitalLoanInternalCobApiApi;
import com.corevance.client.feign.services.WorkingCapitalLoanNearBreachActionsApi;
import com.corevance.client.feign.services.WorkingCapitalLoanOriginatorsApi;
import com.corevance.client.feign.services.WorkingCapitalLoanProductsApi;
import com.corevance.client.feign.services.WorkingCapitalLoanTransactionsApi;
import com.corevance.client.feign.services.WorkingCapitalLoansApi;
import com.corevance.client.feign.services.WorkingCapitalNearBreachApi;
import com.corevance.client.feign.services.WorkingDaysApi;

/**
 * Main entry point for creating Feign-based clients for the Corevance API.
 * <p>
 * Example usage:
 *
 * <pre>
 * {@code
 *
 * CorevanceFeignClient client = CorevanceFeignClient.builder().baseUrl("https://localhost:8443/corevance-provider/api/v1")
 *         .credentials("username", "password").build();
 *
 * // Access API clients
 * ClientApi clientsApi = client.clients();
 * List<ClientData> clients = clientsApi.retrieveAll();
 * }
 * </pre>
 */
public final class CorevanceFeignClient {

    private final CorevanceFeignClientConfig config;

    private CorevanceFeignClient(Builder builder) {
        this.config = builder.configBuilder.build();
    }

    /**
     * Creates a new builder for configuring a CorevanceFeignClient.
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new client for the specified API interface.
     *
     * @param <T>
     *            The API interface type
     * @param apiType
     *            The API interface class
     * @return A configured Feign client for the specified API
     */
    public <T> T create(Class<T> apiType) {
        return config.createClient(apiType);
    }

    public AccountNumberFormatApi accountNumberFormat() {
        return create(AccountNumberFormatApi.class);
    }

    public AccountTransfersApi accountTransfers() {
        return create(AccountTransfersApi.class);
    }

    public AccountingClosureApi accountingClosure() {
        return create(AccountingClosureApi.class);
    }

    public AccountingRulesApi accountingRules() {
        return create(AccountingRulesApi.class);
    }

    public AdhocQueryApiApi adhocQuery() {
        return create(AdhocQueryApiApi.class);
    }

    public AuditsApi audits() {
        return create(AuditsApi.class);
    }

    public AuthenticationHttpBasicApi authenticationHttpBasic() {
        return create(AuthenticationHttpBasicApi.class);
    }

    public BatchApiApi batch() {
        return create(BatchApiApi.class);
    }

    public BulkImportApi bulkImport() {
        return create(BulkImportApi.class);
    }

    public BulkLoansApi bulkLoans() {
        return create(BulkLoansApi.class);
    }

    public BusinessDateManagementApi businessDateManagement() {
        return create(BusinessDateManagementApi.class);
    }

    public BusinessStepConfigurationApi businessStepConfiguration() {
        return create(BusinessStepConfigurationApi.class);
    }

    public CacheApi cache() {
        return create(CacheApi.class);
    }

    public CalendarApi calendar() {
        return create(CalendarApi.class);
    }

    public CashierJournalsApi cashierJournals() {
        return create(CashierJournalsApi.class);
    }

    public CashiersApi cashiers() {
        return create(CashiersApi.class);
    }

    public CentersApi centers() {
        return create(CentersApi.class);
    }

    public ChargesApi charges() {
        return create(ChargesApi.class);
    }

    public ClientApi clients() {
        return create(ClientApi.class);
    }

    public ClientChargesApi clientCharges() {
        return create(ClientChargesApi.class);
    }

    public ClientCollateralManagementApi clientCollateralManagement() {
        return create(ClientCollateralManagementApi.class);
    }

    public ClientFamilyMemberApi clientFamilyMember() {
        return create(ClientFamilyMemberApi.class);
    }

    public ClientIdentifierApi clientIdentifier() {
        return create(ClientIdentifierApi.class);
    }

    public ClientSearchV2Api clientSearchV2() {
        return create(ClientSearchV2Api.class);
    }

    public ClientTransactionApi clientTransaction() {
        return create(ClientTransactionApi.class);
    }

    public ClientsAddressApi clientsAddress() {
        return create(ClientsAddressApi.class);
    }

    public CodeValuesApi codeValues() {
        return create(CodeValuesApi.class);
    }

    public CodesApi codes() {
        return create(CodesApi.class);
    }

    public CollateralManagementApi collateralManagement() {
        return create(CollateralManagementApi.class);
    }

    public CollectionSheetApi collectionSheet() {
        return create(CollectionSheetApi.class);
    }

    public CreditBureauConfigurationApi creditBureauConfiguration() {
        return create(CreditBureauConfigurationApi.class);
    }

    public CurrencyApi currency() {
        return create(CurrencyApi.class);
    }

    public DataTablesApi dataTables() {
        return create(DataTablesApi.class);
    }

    public DefaultApi defaultApi() {
        return create(DefaultApi.class);
    }

    public DelinquencyRangeAndBucketsManagementApi delinquencyRangeAndBucketsManagement() {
        return create(DelinquencyRangeAndBucketsManagementApi.class);
    }

    public DepositAccountOnHoldFundTransactionsApi depositAccountOnHoldFundTransactions() {
        return create(DepositAccountOnHoldFundTransactionsApi.class);
    }

    public DocumentsApi documents() {
        return create(DocumentsApi.class);
    }

    public DocumentsApiFixed documentsFixed() {
        return create(DocumentsApiFixed.class);
    }

    public EntityDataTableApi entityDataTable() {
        return create(EntityDataTableApi.class);
    }

    public EntityFieldConfigurationApi entityFieldConfiguration() {
        return create(EntityFieldConfigurationApi.class);
    }

    public ExternalAssetOwnerLoanProductAttributesApi externalAssetOwnerLoanProductAttributes() {
        return create(ExternalAssetOwnerLoanProductAttributesApi.class);
    }

    public ExternalAssetOwnersApi externalAssetOwners() {
        return create(ExternalAssetOwnersApi.class);
    }

    public ExternalEventConfigurationApi externalEventConfiguration() {
        return create(ExternalEventConfigurationApi.class);
    }

    public ExternalServicesApi externalServices() {
        return create(ExternalServicesApi.class);
    }

    public FetchAuthenticatedUserDetailsApi fetchAuthenticatedUserDetails() {
        return create(FetchAuthenticatedUserDetailsApi.class);
    }

    public CorevanceEntityApi corevanceEntity() {
        return create(CorevanceEntityApi.class);
    }

    public FixedDepositAccountApi fixedDepositAccount() {
        return create(FixedDepositAccountApi.class);
    }

    public FixedDepositAccountTransactionsApi fixedDepositAccountTransactions() {
        return create(FixedDepositAccountTransactionsApi.class);
    }

    public FixedDepositProductApi fixedDepositProduct() {
        return create(FixedDepositProductApi.class);
    }

    public FloatingRatesApi floatingRates() {
        return create(FloatingRatesApi.class);
    }

    public FundsApi funds() {
        return create(FundsApi.class);
    }

    public GeneralLedgerAccountApi generalLedgerAccount() {
        return create(GeneralLedgerAccountApi.class);
    }

    public GlobalConfigurationApi globalConfiguration() {
        return create(GlobalConfigurationApi.class);
    }

    public GroupsApi groups() {
        return create(GroupsApi.class);
    }

    public GroupsLevelApi groupsLevel() {
        return create(GroupsLevelApi.class);
    }

    public GuarantorsApi guarantors() {
        return create(GuarantorsApi.class);
    }

    public HolidaysApi holidays() {
        return create(HolidaysApi.class);
    }

    public HooksApi hooks() {
        return create(HooksApi.class);
    }

    public ImagesApi images() {
        return create(ImagesApi.class);
    }

    public InlineJobApi inlineJob() {
        return create(InlineJobApi.class);
    }

    public InstanceModeApi instanceMode() {
        return create(InstanceModeApi.class);
    }

    public InterOperationApi interOperation() {
        return create(InterOperationApi.class);
    }

    public InterestRateChartApi interestRateChart() {
        return create(InterestRateChartApi.class);
    }

    public InterestRateSlabAKAInterestBandsApi interestRateSlabAKAInterestBands() {
        return create(InterestRateSlabAKAInterestBandsApi.class);
    }

    public InternalCobApi internalCob() {
        return create(InternalCobApi.class);
    }

    public JournalEntriesApi journalEntries() {
        return create(JournalEntriesApi.class);
    }

    public LikelihoodApi likelihood() {
        return create(LikelihoodApi.class);
    }

    public ListReportMailingJobHistoryApi listReportMailingJobHistory() {
        return create(ListReportMailingJobHistoryApi.class);
    }

    public LoanAccountLockApi loanAccountLock() {
        return create(LoanAccountLockApi.class);
    }

    public LoanBuyDownFeesApi loanBuyDownFees() {
        return create(LoanBuyDownFeesApi.class);
    }

    public LoanCapitalizedIncomeApi loanCapitalizedIncome() {
        return create(LoanCapitalizedIncomeApi.class);
    }

    public LoanChargesApi loanCharges() {
        return create(LoanChargesApi.class);
    }

    public LoanCobCatchUpApi loanCobCatchUp() {
        return create(LoanCobCatchUpApi.class);
    }

    public LoanCollateralApi loanCollateral() {
        return create(LoanCollateralApi.class);
    }

    public LoanCollateralManagementApi loanCollateralManagement() {
        return create(LoanCollateralManagementApi.class);
    }

    public LoanDisbursementDetailsApi loanDisbursementDetails() {
        return create(LoanDisbursementDetailsApi.class);
    }

    public LoanInterestPauseApi loanInterestPause() {
        return create(LoanInterestPauseApi.class);
    }

    public LoanOriginatorsApi loanOriginators() {
        return create(LoanOriginatorsApi.class);
    }

    public LoanProductsApi loanProducts() {
        return create(LoanProductsApi.class);
    }

    public LoanReschedulingApi loanRescheduling() {
        return create(LoanReschedulingApi.class);
    }

    public LoanTransactionsApi loanTransactions() {
        return create(LoanTransactionsApi.class);
    }

    public LoansApi loans() {
        return create(LoansApi.class);
    }

    public LoansPointInTimeApi loansPointInTime() {
        return create(LoansPointInTimeApi.class);
    }

    public MakerCheckerOr4EyeFunctionalityApi makerCheckerOr4EyeFunctionality() {
        return create(MakerCheckerOr4EyeFunctionalityApi.class);
    }

    public MappingFinancialActivitiesToAccountsApi mappingFinancialActivitiesToAccounts() {
        return create(MappingFinancialActivitiesToAccountsApi.class);
    }

    public MeetingsApi meetings() {
        return create(MeetingsApi.class);
    }

    public MixMappingApi mixMapping() {
        return create(MixMappingApi.class);
    }

    public MixReportApi mixReport() {
        return create(MixReportApi.class);
    }

    public MixTaxonomyApi mixTaxonomy() {
        return create(MixTaxonomyApi.class);
    }

    public NotesApi notes() {
        return create(NotesApi.class);
    }

    public NotificationApi notification() {
        return create(NotificationApi.class);
    }

    public OfficesApi offices() {
        return create(OfficesApi.class);
    }

    public PasswordPreferencesApi passwordPreferences() {
        return create(PasswordPreferencesApi.class);
    }

    public PaymentTypeApi paymentType() {
        return create(PaymentTypeApi.class);
    }

    public PeriodicAccrualAccountingApi periodicAccrualAccounting() {
        return create(PeriodicAccrualAccountingApi.class);
    }

    public PermissionsApi permissions() {
        return create(PermissionsApi.class);
    }

    public PovertyLineApi povertyLine() {
        return create(PovertyLineApi.class);
    }

    public ProductMixApi productMix() {
        return create(ProductMixApi.class);
    }

    public ProductsApi products() {
        return create(ProductsApi.class);
    }

    public ProgressiveLoanApi progressiveLoan() {
        return create(ProgressiveLoanApi.class);
    }

    public ProvisioningCategoryApi provisioningCategory() {
        return create(ProvisioningCategoryApi.class);
    }

    public ProvisioningCriteriaApi provisioningCriteria() {
        return create(ProvisioningCriteriaApi.class);
    }

    public ProvisioningEntriesApi provisioningEntries() {
        return create(ProvisioningEntriesApi.class);
    }

    public RateApi rate() {
        return create(RateApi.class);
    }

    public RecurringDepositAccountApi recurringDepositAccount() {
        return create(RecurringDepositAccountApi.class);
    }

    public RecurringDepositAccountTransactionsApi recurringDepositAccountTransactions() {
        return create(RecurringDepositAccountTransactionsApi.class);
    }

    public RecurringDepositProductApi recurringDepositProduct() {
        return create(RecurringDepositProductApi.class);
    }

    public RepaymentWithPostDatedChecksApi repaymentWithPostDatedChecks() {
        return create(RepaymentWithPostDatedChecksApi.class);
    }

    public ReportMailingJobsApi reportMailingJobs() {
        return create(ReportMailingJobsApi.class);
    }

    public ReportsApi reports() {
        return create(ReportsApi.class);
    }

    public RescheduleLoansApi rescheduleLoans() {
        return create(RescheduleLoansApi.class);
    }

    public RolesApi roles() {
        return create(RolesApi.class);
    }

    public RunReportsApi runReports() {
        return create(RunReportsApi.class);
    }

    public SavingsAccountApi savingsAccount() {
        return create(SavingsAccountApi.class);
    }

    public SavingsAccountTransactionsApi savingsAccountTransactions() {
        return create(SavingsAccountTransactionsApi.class);
    }

    public SavingsChargesApi savingsCharges() {
        return create(SavingsChargesApi.class);
    }

    public SavingsProductApi savingsProduct() {
        return create(SavingsProductApi.class);
    }

    public SchedulerApi scheduler() {
        return create(SchedulerApi.class);
    }

    public SchedulerJobApi schedulerJob() {
        return create(SchedulerJobApi.class);
    }

    public ScoreCardApi scoreCard() {
        return create(ScoreCardApi.class);
    }

    public SearchApiApi search() {
        return create(SearchApiApi.class);
    }

    public ShareAccountApi shareAccount() {
        return create(ShareAccountApi.class);
    }

    public SmsApi sms() {
        return create(SmsApi.class);
    }

    public SpmApiLookUpTableApi spmApiLookUpTable() {
        return create(SpmApiLookUpTableApi.class);
    }

    public SpmSurveysApi spmSurveys() {
        return create(SpmSurveysApi.class);
    }

    public StaffApi staff() {
        return create(StaffApi.class);
    }

    public StandingInstructionsApi standingInstructions() {
        return create(StandingInstructionsApi.class);
    }

    public StandingInstructionsHistoryApi standingInstructionsHistory() {
        return create(StandingInstructionsHistoryApi.class);
    }

    public SurveyApi survey() {
        return create(SurveyApi.class);
    }

    public TaxComponentsApi taxComponents() {
        return create(TaxComponentsApi.class);
    }

    public TaxGroupApi taxGroup() {
        return create(TaxGroupApi.class);
    }

    public TellerCashManagementApi tellerCashManagement() {
        return create(TellerCashManagementApi.class);
    }

    public TwoFactorApi twoFactor() {
        return create(TwoFactorApi.class);
    }

    public TemplatesApi templates() {
        return create(TemplatesApi.class);
    }

    public UsersApi users() {
        return create(UsersApi.class);
    }

    public WorkingCapitalLoanProductsApi workingCapitalLoanProducts() {
        return create(WorkingCapitalLoanProductsApi.class);
    }

    public WorkingCapitalLoanAccountLockApi workingCapitalLoanAccountLock() {
        return create(WorkingCapitalLoanAccountLockApi.class);
    }

    public WorkingCapitalLoanCobCatchUpApi workingCapitalLoanCobCatchUpApi() {
        return create(WorkingCapitalLoanCobCatchUpApi.class);
    }

    public WorkingCapitalLoanDelinquencyActionsApi workingCapitalLoanDelinquencyActions() {
        return create(WorkingCapitalLoanDelinquencyActionsApi.class);
    }

    public WorkingCapitalLoanDelinquencyRangeScheduleApi workingCapitalLoanDelinquencyRangeSchedule() {
        return create(WorkingCapitalLoanDelinquencyRangeScheduleApi.class);
    }

    public WorkingCapitalLoanBreachScheduleApi workingCapitalLoanBreachSchedule() {
        return create(WorkingCapitalLoanBreachScheduleApi.class);
    }

    public WorkingCapitalLoanBreachActionsApi workingCapitalLoanBreachActions() {
        return create(WorkingCapitalLoanBreachActionsApi.class);
    }

    public InternalWorkingCapitalLoansApi internalWorkingCapitalLoans() {
        return create(InternalWorkingCapitalLoansApi.class);
    }

    public WorkingCapitalLoansApi workingCapitalLoans() {
        return create(WorkingCapitalLoansApi.class);
    }

    public WorkingCapitalLoanChargesApi workingCapitalLoanCharges() {
        return create(WorkingCapitalLoanChargesApi.class);
    }

    public WorkingCapitalLoanTransactionsApi workingCapitalLoanTransactions() {
        return create(WorkingCapitalLoanTransactionsApi.class);
    }

    public WorkingCapitalLoanInternalCobApiApi workingCapitalLoanInternalCobApi() {
        return create(WorkingCapitalLoanInternalCobApiApi.class);
    }

    public WorkingCapitalBreachApi workingCapitalBreaches() {
        return create(WorkingCapitalBreachApi.class);
    }

    public WorkingCapitalNearBreachApi workingCapitalNearBreaches() {
        return create(WorkingCapitalNearBreachApi.class);
    }

    public WorkingCapitalLoanNearBreachActionsApi workingCapitalLoanNearBreachActions() {
        return create(WorkingCapitalLoanNearBreachActionsApi.class);
    }

    public WorkingCapitalLoanOriginatorsApi workingCapitalLoanOriginators() {
        return create(WorkingCapitalLoanOriginatorsApi.class);
    }

    public WorkingDaysApi workingDays() {
        return create(WorkingDaysApi.class);
    }

    /**
     * Builder for creating and configuring a CorevanceFeignClient.
     */
    public static class Builder {

        private final CorevanceFeignClientConfig.Builder configBuilder = CorevanceFeignClientConfig.builder();

        /**
         * Sets the base URL for the Corevance API.
         *
         * @param baseUrl
         *            The base URL (e.g., "https://localhost:8443/corevance-provider/api/v1")
         * @return This builder instance
         */
        public Builder baseUrl(String baseUrl) {
            configBuilder.baseUrl(baseUrl);
            return this;
        }

        /**
         * Sets the credentials for Basic Authentication.
         *
         * @param username
         *            The username
         * @param password
         *            The password
         * @return This builder instance
         */
        public Builder credentials(String username, String password) {
            configBuilder.credentials(username, password);
            return this;
        }

        /**
         * Sets the connection timeout.
         *
         * @param timeout
         *            The timeout value
         * @param unit
         *            The time unit
         * @return This builder instance
         */
        public Builder connectTimeout(int timeout, java.util.concurrent.TimeUnit unit) {
            configBuilder.connectTimeout(timeout, unit);
            return this;
        }

        /**
         * Sets the read timeout.
         *
         * @param timeout
         *            The timeout value
         * @param unit
         *            The time unit
         * @return This builder instance
         */
        public Builder readTimeout(int timeout, java.util.concurrent.TimeUnit unit) {
            configBuilder.readTimeout(timeout, unit);
            return this;
        }

        /**
         * Enables or disables debug logging.
         *
         * @param enabled
         *            true to enable debug logging, false to disable
         * @return This builder instance
         */
        public Builder debug(boolean enabled) {
            configBuilder.debugEnabled(enabled);
            return this;
        }

        /**
         * Disables SSL certificate verification. Use only for testing with self-signed certificates.
         *
         * @param disable
         *            true to disable SSL verification, false to enable
         * @return This builder instance
         */
        public Builder disableSslVerification(boolean disable) {
            configBuilder.disableSslVerification(disable);
            return this;
        }

        public Builder tenantId(String tenantId) {
            configBuilder.tenantId(tenantId);
            return this;
        }

        /**
         * Sets the connection time-to-live (TTL) for connection pool recycling.
         *
         * @param ttl
         *            The time-to-live value
         * @param unit
         *            The time unit
         * @return This builder instance
         */
        public Builder connectionTimeToLive(long ttl, java.util.concurrent.TimeUnit unit) {
            configBuilder.connectionTimeToLive(ttl, unit);
            return this;
        }

        /**
         * Sets the idle connection eviction time.
         *
         * @param time
         *            The eviction time value
         * @param unit
         *            The time unit
         * @return This builder instance
         */
        public Builder idleConnectionEvictionTime(long time, java.util.concurrent.TimeUnit unit) {
            configBuilder.idleConnectionEvictionTime(time, unit);
            return this;
        }

        /**
         * Sets the maximum total connections in the pool.
         *
         * @param max
         *            Maximum total connections
         * @return This builder instance
         */
        public Builder maxConnections(int max) {
            configBuilder.maxConnTotal(max);
            return this;
        }

        /**
         * Sets the maximum connections per route.
         *
         * @param max
         *            Maximum connections per route
         * @return This builder instance
         */
        public Builder maxConnectionsPerRoute(int max) {
            configBuilder.maxConnPerRoute(max);
            return this;
        }

        /**
         * Sets the HTTP client type.
         *
         * @param clientType
         *            The HTTP client type (APACHE or OKHTTP)
         * @return This builder instance
         */
        public Builder httpClientType(CorevanceFeignClientConfig.HttpClientType clientType) {
            configBuilder.httpClientType(clientType);
            return this;
        }

        /**
         * Builds a new CorevanceFeignClient with the current configuration.
         *
         * @return A new CorevanceFeignClient instance
         */
        public CorevanceFeignClient build() {
            return new CorevanceFeignClient(this);
        }
    }
}
