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
package com.corevance.portfolio.delinquency.service;

import static com.corevance.portfolio.loanaccount.domain.Loan.EARLIEST_UNPAID_DATE;
import static com.corevance.portfolio.loanaccount.domain.Loan.NEXT_UNPAID_DUE_DATE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.infrastructure.core.service.MathUtil;
import com.corevance.infrastructure.core.service.ThreadLocalContextUtil;
import com.corevance.organisation.monetary.domain.MoneyHelper;
import com.corevance.portfolio.delinquency.data.DelinquencyBucketData;
import com.corevance.portfolio.delinquency.data.DelinquencyRangeData;
import com.corevance.portfolio.delinquency.data.LoanDelinquencyTagHistoryData;
import com.corevance.portfolio.delinquency.data.LoanInstallmentDelinquencyTagData;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucket;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucketRepository;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucketType;
import com.corevance.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRuleRepository;
import com.corevance.portfolio.delinquency.domain.DelinquencyRange;
import com.corevance.portfolio.delinquency.domain.DelinquencyRangeRepository;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyAction;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyTagHistory;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import com.corevance.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import com.corevance.portfolio.delinquency.exception.DelinquencyBucketNotFoundException;
import com.corevance.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import com.corevance.portfolio.delinquency.helper.InstallmentDelinquencyAggregator;
import com.corevance.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import com.corevance.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import com.corevance.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import com.corevance.portfolio.delinquency.validator.LoanDelinquencyActionData;
import com.corevance.portfolio.loanaccount.data.CollectionData;
import com.corevance.portfolio.loanaccount.data.DelinquencyPausePeriod;
import com.corevance.portfolio.loanaccount.data.InstallmentLevelDelinquency;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanDisbursementDetails;
import com.corevance.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import com.corevance.portfolio.loanaccount.domain.LoanRepository;
import com.corevance.portfolio.loanaccount.domain.LoanSummary;
import com.corevance.portfolio.loanaccount.domain.LoanTransaction;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanproduct.domain.LoanProduct;
import com.corevance.portfolio.loanproduct.exception.LoanProductGeneralRuleException;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DelinquencyReadPlatformServiceImpl implements DelinquencyReadPlatformService {

    private final DelinquencyRangeRepository repositoryRange;
    private final DelinquencyBucketRepository repositoryBucket;
    private final DelinquencyMinimumPaymentPeriodAndRuleRepository minimumPaymentPeriodAndRuleRepository;
    private final LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory;
    private final DelinquencyRangeMapper mapperRange;
    private final DelinquencyBucketMapper mapperBucket;
    private final LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory;
    private final LoanRepository loanRepository;
    private final LoanDelinquencyDomainService loanDelinquencyDomainService;
    private final LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag;
    private final LoanDelinquencyActionRepository loanDelinquencyActionRepository;
    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanTransactionRepository loanTransactionRepository;
    private final PossibleNextRepaymentCalculationServiceDiscovery possibleNextRepaymentCalculationServiceDiscovery;

    @Override
    public List<DelinquencyRangeData> retrieveAllDelinquencyRanges() {
        final List<DelinquencyRange> delinquencyRangeList = repositoryRange.findAll();
        return mapperRange.map(delinquencyRangeList);
    }

    @Override
    public DelinquencyRangeData retrieveDelinquencyRange(Long delinquencyRangeId) {
        DelinquencyRange delinquencyRangeList = repositoryRange.getReferenceById(delinquencyRangeId);
        return mapperRange.map(delinquencyRangeList);
    }

    @Override
    public List<DelinquencyBucketData> retrieveAllDelinquencyBuckets() {
        final List<DelinquencyBucket> delinquencyRangeList = repositoryBucket.findAll();
        final List<DelinquencyBucketData> result = mapperBucket.map(delinquencyRangeList);
        result.forEach(this::enrichWorkingCapitalConfiguration);
        return result;
    }

    @Override
    public DelinquencyBucketData retrieveDelinquencyBucket(Long delinquencyBucketId) {
        if (!repositoryBucket.existsById(delinquencyBucketId)) {
            throw DelinquencyBucketNotFoundException.notFound(delinquencyBucketId);
        }
        final DelinquencyBucket delinquencyBucket = repositoryBucket.getReferenceById(delinquencyBucketId);
        final DelinquencyBucketData delinquencyBucketData = mapperBucket.map(delinquencyBucket);
        delinquencyBucketData.setRanges(mapperRange.map(delinquencyBucket.getRanges()));
        enrichWorkingCapitalConfiguration(delinquencyBucketData);
        return delinquencyBucketData;
    }

    private void enrichWorkingCapitalConfiguration(DelinquencyBucketData bucketData) {
        if (bucketData != null && DelinquencyBucketType.WORKING_CAPITAL.equals(bucketData.getBucketType()) && bucketData.getId() != null) {
            minimumPaymentPeriodAndRuleRepository.findByBucketId(bucketData.getId())
                    .ifPresent(rule -> bucketData.setMinimumPaymentPeriodAndRule(mapperBucket.map(rule)));
        }
    }

    @Override
    public DelinquencyRangeData retrieveCurrentDelinquencyTag(Long loanId) {
        final Loan loan = this.loanRepository.getReferenceById(loanId);
        Optional<LoanDelinquencyTagHistory> optLoanDelinquencyTag = this.repositoryLoanDelinquencyTagHistory.findByLoanAndLiftedOnDate(loan,
                null);
        return optLoanDelinquencyTag.map(loanDelinquencyTagHistory -> mapperRange.map(loanDelinquencyTagHistory.getDelinquencyRange()))
                .orElse(null);
    }

    @Override
    public Collection<LoanDelinquencyTagHistoryData> retrieveDelinquencyRangeHistory(Long loanId) {
        final Loan loan = this.loanRepository.getReferenceById(loanId);
        final List<LoanDelinquencyTagHistory> loanDelinquencyTagData = this.repositoryLoanDelinquencyTagHistory
                .findByLoanOrderByAddedOnDateDesc(loan);
        return mapperLoanDelinquencyTagHistory.map(loanDelinquencyTagData);
    }

    @Override
    public CollectionData calculateLoanCollectionData(final Long loanId) {
        final Optional<Loan> optLoan = this.loanRepository.findById(loanId);

        CollectionData collectionData = CollectionData.template();
        if (optLoan.isPresent()) {
            final Loan loan = optLoan.get();

            // If the Loan is not Active yet or is cancelled (rejected or withdrawn), return template data
            if (loan.isSubmittedAndPendingApproval() || loan.isApproved() || loan.isCancelled()) {
                if (loan.getLoanProduct() != null && loan.getLoanProduct().isAllowApprovedDisbursedAmountsOverApplied()) {
                    collectionData.setAvailableDisbursementAmountWithOverApplied(calculateAvailableDisbursementAmountWithOverApplied(loan));
                }
                return collectionData;
            }

            final List<LoanDelinquencyAction> savedDelinquencyList = retrieveLoanDelinquencyActions(loanId);
            List<LoanDelinquencyActionData> effectiveDelinquencyList = delinquencyEffectivePauseHelper
                    .calculateEffectiveDelinquencyList(savedDelinquencyList);

            final String nextPaymentDueDateConfig = configurationDomainService.getNextPaymentDateConfigForLoan();

            // Below method calculates delinquency for active loans only and returns template data for Closed or
            // Overpaid
            // loans
            collectionData = loanDelinquencyDomainService.getOverdueCollectionData(loan, effectiveDelinquencyList);
            collectionData.setAvailableDisbursementAmount(calculateAvailableDisbursementAmount(loan));
            collectionData.setAvailableDisbursementAmountWithOverApplied(calculateAvailableDisbursementAmountWithOverApplied(loan));
            collectionData.setNextPaymentDueDate(possibleNextRepaymentDate(nextPaymentDueDateConfig, loan));
            PossibleNextRepaymentCalculationService possibleNextRepaymentCalculationService = possibleNextRepaymentCalculationServiceDiscovery
                    .getService(loan);
            if (possibleNextRepaymentCalculationService != null) {
                collectionData.setNextPaymentAmount(
                        possibleNextRepaymentCalculationService.possibleNextRepaymentAmount(loan, collectionData.getNextPaymentDueDate()));
            }

            final LoanTransaction lastPayment = loan.getLastPaymentTransaction();
            if (lastPayment != null) {
                collectionData.setLastPaymentDate(lastPayment.getTransactionDate());
                collectionData.setLastPaymentAmount(lastPayment.getAmount());
            }

            final LoanTransaction lastRepaymentTransaction = loan.getLastRepaymentOrDownPaymentTransaction();
            if (lastRepaymentTransaction != null) {
                collectionData.setLastRepaymentDate(lastRepaymentTransaction.getTransactionDate());
                collectionData.setLastRepaymentAmount(lastRepaymentTransaction.getAmount());
            }

            enrichWithDelinquencyPausePeriodInfo(collectionData, effectiveDelinquencyList, ThreadLocalContextUtil.getBusinessDate());

            if (optLoan.get().isEnableInstallmentLevelDelinquency()) {
                addInstallmentLevelDelinquencyData(collectionData, loanId);
            }
        }

        return collectionData;
    }

    @Override
    public BigDecimal calculateAvailableDisbursementAmountWithOverApplied(@NonNull final Loan loan) {
        final LoanProduct loanProduct = loan.getLoanProduct();

        // Start with approved principal
        BigDecimal approvedWithOverApplied = loan.getApprovedPrincipal();

        // If over applied amount is enabled, calculate the maximum allowed amount
        if (loanProduct != null && loanProduct.isAllowApprovedDisbursedAmountsOverApplied()) {
            if (loanProduct.getOverAppliedCalculationType() != null) {
                if ("percentage".equalsIgnoreCase(loanProduct.getOverAppliedCalculationType())) {
                    final BigDecimal overAppliedNumber = BigDecimal.valueOf(loanProduct.getOverAppliedNumber());
                    final BigDecimal totalPercentage = BigDecimal.valueOf(1)
                            .add(overAppliedNumber.divide(BigDecimal.valueOf(100), MoneyHelper.getMathContext()));
                    approvedWithOverApplied = loan.getProposedPrincipal().multiply(totalPercentage);
                } else {
                    approvedWithOverApplied = loan.getProposedPrincipal().add(BigDecimal.valueOf(loanProduct.getOverAppliedNumber()));
                }
            } else {
                throw new LoanProductGeneralRuleException("overAppliedCalculationType.must.be.percentage.or.flat",
                        "Over Applied Calculation Type Must Be 'percentage' or 'flat'");
            }
        }

        // Calculate available amount: (approved + over applied) - expected tranches - disbursed - capitalized income
        if (loan.isMultiDisburmentLoan() && loan.getDisbursementDetails() != null) {
            final BigDecimal expectedDisbursementAmount = loan.getDisbursementDetails().stream()
                    .filter(detail -> detail.actualDisbursementDate() == null).map(LoanDisbursementDetails::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            approvedWithOverApplied = approvedWithOverApplied.subtract(expectedDisbursementAmount);
        }

        BigDecimal availableDisbursementAmount = approvedWithOverApplied.subtract(loan.getDisbursedAmount());

        if (loan.getLoanRepaymentScheduleDetail().isEnableIncomeCapitalization()) {
            final LoanSummary loanSummary = loan.getSummary();
            if (loanSummary != null) {
                final BigDecimal totalCapitalizedIncome = MathUtil.nullToZero(loanSummary.getTotalCapitalizedIncome());
                final BigDecimal totalCapitalizedIncomeAdjustment = MathUtil.nullToZero(loanSummary.getTotalCapitalizedIncomeAdjustment());
                final BigDecimal netCapitalizedIncome = totalCapitalizedIncome.subtract(totalCapitalizedIncomeAdjustment);
                availableDisbursementAmount = availableDisbursementAmount.subtract(netCapitalizedIncome);
            }
        }

        // Ensure availableDisbursementAmount is never negative
        return availableDisbursementAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : availableDisbursementAmount;
    }

    private BigDecimal calculateAvailableDisbursementAmount(@NonNull final Loan loan) {
        BigDecimal availableDisbursementAmount = loan.getApprovedPrincipal().subtract(loan.getDisbursedAmount());
        if (loan.getLoanRepaymentScheduleDetail().isEnableIncomeCapitalization()) {
            final LoanSummary loanSummary = loan.getSummary();
            if (loanSummary != null) {
                final BigDecimal totalCapitalizedIncome = MathUtil.nullToZero(loanSummary.getTotalCapitalizedIncome());
                final BigDecimal totalCapitalizedIncomeAdjustment = MathUtil.nullToZero(loanSummary.getTotalCapitalizedIncomeAdjustment());
                final BigDecimal netCapitalizedIncome = totalCapitalizedIncome.subtract(totalCapitalizedIncomeAdjustment);
                availableDisbursementAmount = availableDisbursementAmount.subtract(netCapitalizedIncome);
            }
        }

        // Ensure availableDisbursementAmount is never negative
        return availableDisbursementAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : availableDisbursementAmount;
    }

    private void addInstallmentLevelDelinquencyData(CollectionData collectionData, Long loanId) {
        Collection<LoanInstallmentDelinquencyTagData> loanInstallmentDelinquencyTagData = retrieveLoanInstallmentsCurrentDelinquencyTag(
                loanId);
        if (loanInstallmentDelinquencyTagData != null && !loanInstallmentDelinquencyTagData.isEmpty()) {
            List<InstallmentLevelDelinquency> aggregated = InstallmentDelinquencyAggregator
                    .aggregateAndSort(loanInstallmentDelinquencyTagData);
            collectionData.setInstallmentLevelDelinquency(aggregated);
        }
    }

    void enrichWithDelinquencyPausePeriodInfo(CollectionData collectionData, Collection<LoanDelinquencyActionData> effectiveDelinquencyList,
            LocalDate businessDate) {
        List<DelinquencyPausePeriod> result = effectiveDelinquencyList.stream() //
                .sorted(Comparator.comparing(LoanDelinquencyActionData::getStartDate)) //
                .map(lda -> toDelinquencyPausePeriod(businessDate, lda)).toList(); //
        collectionData.setDelinquencyPausePeriods(result);
    }

    @NonNull
    private static DelinquencyPausePeriod toDelinquencyPausePeriod(LocalDate businessDate, LoanDelinquencyActionData lda) {
        return new DelinquencyPausePeriod(!lda.getStartDate().isAfter(businessDate) && !businessDate.isAfter(lda.getEndDate()),
                lda.getStartDate(), lda.getEndDate());
    }

    @Override
    public Collection<LoanInstallmentDelinquencyTagData> retrieveLoanInstallmentsCurrentDelinquencyTag(Long loanId) {
        return repositoryLoanInstallmentDelinquencyTag.findInstallmentDelinquencyTags(loanId);
    }

    @Override
    public List<LoanDelinquencyAction> retrieveLoanDelinquencyActions(Long loanId) {
        final Optional<Loan> optLoan = this.loanRepository.findById(loanId);
        if (optLoan.isPresent()) {
            return loanDelinquencyActionRepository.findByLoanOrderById(optLoan.get());
        }
        return List.of();
    }

    private LocalDate possibleNextRepaymentDate(final String nextPaymentDueDateConfig, final Loan loan) {
        if (nextPaymentDueDateConfig == null) {
            return null;
        }
        return switch (nextPaymentDueDateConfig.toLowerCase()) {
            case EARLIEST_UNPAID_DATE -> getEarliestUnpaidInstallmentDate(loan);
            case NEXT_UNPAID_DUE_DATE -> getNextUnpaidInstallmentDueDate(loan);
            default -> null;
        };
    }

    private LocalDate getEarliestUnpaidInstallmentDate(final Loan loan) {
        LocalDate earliestUnpaidInstallmentDate = DateUtils.getBusinessLocalDate();
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        for (final LoanRepaymentScheduleInstallment installment : installments) {
            if (installment.isNotFullyPaidOff()) {
                earliestUnpaidInstallmentDate = installment.getDueDate();
                break;
            }
        }

        final LocalDate lastTransactionDate = loanTransactionRepository.findLastRepaymentLikeTransactionDate(loan).orElse(null);

        LocalDate possibleNextRepaymentDate = earliestUnpaidInstallmentDate;
        if (DateUtils.isAfter(lastTransactionDate, earliestUnpaidInstallmentDate)) {
            possibleNextRepaymentDate = lastTransactionDate;
        }

        return possibleNextRepaymentDate;
    }

    private LocalDate getNextUnpaidInstallmentDueDate(final Loan loan) {
        List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments();
        LocalDate currentBusinessDate = DateUtils.getBusinessLocalDate();
        LocalDate expectedMaturityDate = loan.determineExpectedMaturityDate();
        LocalDate nextUnpaidInstallmentDate = expectedMaturityDate;

        for (final LoanRepaymentScheduleInstallment installment : installments) {
            boolean isCurrentDateBeforeInstallmentAndLoanPeriod = DateUtils.isBefore(currentBusinessDate, installment.getDueDate())
                    && DateUtils.isBefore(currentBusinessDate, expectedMaturityDate);
            if (installment.isDownPayment()) {
                isCurrentDateBeforeInstallmentAndLoanPeriod = DateUtils.isEqual(currentBusinessDate, installment.getDueDate())
                        && DateUtils.isBefore(currentBusinessDate, expectedMaturityDate);
            }
            if (isCurrentDateBeforeInstallmentAndLoanPeriod && installment.isNotFullyPaidOff()) {
                nextUnpaidInstallmentDate = installment.getDueDate();
                break;
            }
        }
        return nextUnpaidInstallmentDate;
    }

}
