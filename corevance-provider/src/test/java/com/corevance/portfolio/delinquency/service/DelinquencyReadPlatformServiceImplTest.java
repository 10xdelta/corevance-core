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

import static java.time.Month.JANUARY;
import static com.corevance.portfolio.delinquency.domain.DelinquencyAction.PAUSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.portfolio.delinquency.domain.DelinquencyBucketRepository;
import com.corevance.portfolio.delinquency.domain.DelinquencyMinimumPaymentPeriodAndRuleRepository;
import com.corevance.portfolio.delinquency.domain.DelinquencyRangeRepository;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyAction;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyActionRepository;
import com.corevance.portfolio.delinquency.domain.LoanDelinquencyTagHistoryRepository;
import com.corevance.portfolio.delinquency.domain.LoanInstallmentDelinquencyTagRepository;
import com.corevance.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import com.corevance.portfolio.delinquency.mapper.DelinquencyBucketMapper;
import com.corevance.portfolio.delinquency.mapper.DelinquencyRangeMapper;
import com.corevance.portfolio.delinquency.mapper.LoanDelinquencyTagMapper;
import com.corevance.portfolio.delinquency.validator.LoanDelinquencyActionData;
import com.corevance.portfolio.loanaccount.data.CollectionData;
import com.corevance.portfolio.loanaccount.data.DelinquencyPausePeriod;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanRepository;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanproduct.domain.LoanProduct;
import com.corevance.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelinquencyReadPlatformServiceImplTest {

    @Mock
    private DelinquencyRangeRepository repositoryRange;

    @Mock
    private DelinquencyBucketRepository repositoryBucket;
    @Mock
    private LoanDelinquencyTagHistoryRepository repositoryLoanDelinquencyTagHistory;
    @Mock
    private DelinquencyRangeMapper mapperRange;
    @Mock
    private DelinquencyBucketMapper mapperBucket;

    @Mock
    private LoanDelinquencyTagMapper mapperLoanDelinquencyTagHistory;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanDelinquencyDomainService loanDelinquencyDomainService;

    @Mock
    private LoanInstallmentDelinquencyTagRepository repositoryLoanInstallmentDelinquencyTag;

    @Mock
    private LoanDelinquencyActionRepository loanDelinquencyActionRepository;
    @Mock
    private DelinquencyMinimumPaymentPeriodAndRuleRepository minimumPaymentPeriodAndRuleRepository;
    @Mock
    private DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private PossibleNextRepaymentCalculationServiceDiscovery possibleNextRepaymentCalculationServiceDiscovery;

    @InjectMocks
    private DelinquencyReadPlatformServiceImpl underTest;

    @Test
    public void testNoEnrichmentWhenThereIsNoDelinquencyAction() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of();

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        Assertions.assertTrue(collectionData.getDelinquencyPausePeriods().isEmpty());
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentlyInPauseFirstDay() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of(
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 13))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20))));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        // then
        verifyPausePeriods(collectionData, //
                pausePeriod(false, "2023-01-10", "2023-01-11"), //
                pausePeriod(true, "2023-01-12", "2023-01-13"), //
                pausePeriod(false, "2023-01-15", "2023-01-20") //
        );
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentlyInPauseLastDay() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of(
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 13))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20))));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 13));

        verifyPausePeriods(collectionData, //
                pausePeriod(false, "2023-01-10", "2023-01-11"), //
                pausePeriod(true, "2023-01-12", "2023-01-13"), //
                pausePeriod(false, "2023-01-15", "2023-01-20") //
        );
    }

    @Test
    public void testMultiplePausesWithoutResumeActionCurrentBusinessDateBetweenStartAndEndDate() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of(
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 12), LocalDate.of(2023, JANUARY, 14))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20))));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 13));

        verifyPausePeriods(collectionData, //
                pausePeriod(false, "2023-01-10", "2023-01-11"), //
                pausePeriod(true, "2023-01-12", "2023-01-14"), //
                pausePeriod(false, "2023-01-15", "2023-01-20") //
        );
    }

    @Test
    public void testMultiplePausesWithoutResumeCurrentBusinessDateIsNotOverlappingWithAnyOfThePauses() {
        // given
        CollectionData collectionData = CollectionData.template();
        Collection<LoanDelinquencyActionData> delinquencyActions = List.of(
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 10), LocalDate.of(2023, JANUARY, 11))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 13), LocalDate.of(2023, JANUARY, 14))),
                new LoanDelinquencyActionData(
                        new LoanDelinquencyAction(null, PAUSE, LocalDate.of(2023, JANUARY, 15), LocalDate.of(2023, JANUARY, 20))));

        // when
        underTest.enrichWithDelinquencyPausePeriodInfo(collectionData, delinquencyActions, LocalDate.of(2023, JANUARY, 12));

        // then
        verifyPausePeriods(collectionData, //
                pausePeriod(false, "2023-01-10", "2023-01-11"), //
                pausePeriod(false, "2023-01-13", "2023-01-14"), //
                pausePeriod(false, "2023-01-15", "2023-01-20") //
        );
    }

    private void verifyPausePeriods(CollectionData collectionData, DelinquencyPausePeriod... pausePeriods) {
        if (pausePeriods.length > 0) {
            Assertions.assertEquals(Arrays.asList(pausePeriods), collectionData.getDelinquencyPausePeriods());
        } else {
            Assertions.assertNull(collectionData.getDelinquencyPausePeriods());
        }
    }

    private DelinquencyPausePeriod pausePeriod(boolean active, String startDate, String endDate) {
        return new DelinquencyPausePeriod(active, LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    @Test
    public void testCalculateLoanCollectionDataWhenLoanProductIsNullAndLoanIsPendingApproval() {
        // given
        Loan loan = mock(Loan.class);
        when(loan.isSubmittedAndPendingApproval()).thenReturn(true);
        when(loan.getLoanProduct()).thenReturn(null);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        // when
        CollectionData result = underTest.calculateLoanCollectionData(1L);
        // then
        assertThat(result).isNotNull();
        assertThat(result.getAvailableDisbursementAmountWithOverApplied()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void testCalculateAvailableDisbursementAmountWithOverAppliedWhenLoanProductIsNull() {
        // given
        Loan loan = mock(Loan.class);
        when(loan.getLoanProduct()).thenReturn(null);
        when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.valueOf(5000));
        when(loan.getDisbursedAmount()).thenReturn(BigDecimal.valueOf(2000));
        when(loan.getLoanRepaymentScheduleDetail()).thenReturn(mock(LoanProductRelatedDetail.class));
        // when
        BigDecimal result = underTest.calculateAvailableDisbursementAmountWithOverApplied(loan);
        // then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    public void testCalculateAvailableDisbursementAmountWithOverAppliedWhenLoanProductPresentAndOverApplyEnabled() {
        // given
        Loan loan = mock(Loan.class);
        LoanProduct loanProduct = mock(LoanProduct.class);
        when(loan.getLoanProduct()).thenReturn(loanProduct);
        when(loanProduct.isAllowApprovedDisbursedAmountsOverApplied()).thenReturn(true);
        when(loanProduct.getOverAppliedCalculationType()).thenReturn("flat");
        when(loanProduct.getOverAppliedNumber()).thenReturn(500);
        when(loan.getProposedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
        when(loan.getApprovedPrincipal()).thenReturn(BigDecimal.valueOf(10000));
        when(loan.getDisbursedAmount()).thenReturn(BigDecimal.ZERO);
        when(loan.getLoanRepaymentScheduleDetail()).thenReturn(mock(LoanProductRelatedDetail.class));
        // when
        BigDecimal result = underTest.calculateAvailableDisbursementAmountWithOverApplied(loan);
        // then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(10500));
    }
}
