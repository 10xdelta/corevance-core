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
package com.corevance.portfolio.account.jobs.executestandinginstructions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import com.corevance.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import com.corevance.infrastructure.codes.domain.CodeValue;
import com.corevance.infrastructure.configuration.api.GlobalConfigurationConstants;
import com.corevance.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import com.corevance.infrastructure.configuration.service.ConfigurationReadPlatformService;
import com.corevance.organisation.office.domain.Office;
import com.corevance.portfolio.account.service.AccountNumberGenerator;
import com.corevance.portfolio.client.domain.Client;
import com.corevance.portfolio.client.domain.ClientRepository;
import com.corevance.portfolio.loanaccount.domain.Loan;
import com.corevance.portfolio.loanaccount.domain.LoanRepository;
import com.corevance.portfolio.loanproduct.domain.LoanProduct;
import com.corevance.portfolio.savings.domain.SavingsAccount;
import com.corevance.portfolio.savings.domain.SavingsAccountRepository;
import com.corevance.portfolio.savings.domain.SavingsProduct;
import com.corevance.portfolio.shareaccounts.domain.ShareAccount;
import com.corevance.portfolio.shareproducts.domain.ShareProduct;
import com.corevance.portfolio.workingcapitalloan.repository.WorkingCapitalLoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountNumberGeneratorTest {

    private ConfigurationReadPlatformService configService;
    private ClientRepository clientRepo;
    private LoanRepository loanRepo;
    private SavingsAccountRepository savingsRepo;

    private AccountNumberGenerator generator;

    @BeforeEach
    public void setup() {
        configService = mock(ConfigurationReadPlatformService.class);
        clientRepo = mock(ClientRepository.class);
        loanRepo = mock(LoanRepository.class);
        savingsRepo = mock(SavingsAccountRepository.class);
        final WorkingCapitalLoanRepository workingCapitalLoanRepo = mock(WorkingCapitalLoanRepository.class);

        generator = new AccountNumberGenerator(configService, clientRepo, loanRepo, savingsRepo, workingCapitalLoanRepo);

        GlobalConfigurationPropertyData accountLengthConfig = mock(GlobalConfigurationPropertyData.class);
        when(accountLengthConfig.getValue()).thenReturn(Long.valueOf("9"));
        when(configService.retrieveGlobalConfiguration(GlobalConfigurationConstants.CUSTOM_ACCOUNT_NUMBER_LENGTH))
                .thenReturn(accountLengthConfig);

        GlobalConfigurationPropertyData randomAccountConfig = mock(GlobalConfigurationPropertyData.class);
        when(randomAccountConfig.getValue()).thenReturn(Long.valueOf("0"));
        when(configService.retrieveGlobalConfiguration(GlobalConfigurationConstants.RANDOM_ACCOUNT_NUMBER)).thenReturn(randomAccountConfig);
    }

    @Test
    public void testGenerateClientAccountNumber() {
        Client client = mock(Client.class);
        Office office = mock(Office.class);
        CodeValue clientType = mock(CodeValue.class);

        when(client.getId()).thenReturn(123L);
        when(client.getOffice()).thenReturn(office);
        when(office.getName()).thenReturn("MainOffice");
        when(client.clientType()).thenReturn(clientType);
        when(clientType.getLabel()).thenReturn("Individual");

        AccountNumberFormat format = mock(AccountNumberFormat.class);
        when(format.getPrefixEnum()).thenReturn(null);

        String accountNumber = generator.generate(client, format);
        assertThat(accountNumber).isEqualTo("000000123");
    }

    @Test
    public void testGenerateLoanAccountNumber() {
        Loan loan = mock(Loan.class);
        Office office = mock(Office.class);
        LoanProduct product = mock(LoanProduct.class);

        when(loan.getId()).thenReturn(77L);
        when(loan.getOffice()).thenReturn(office);
        when(office.getName()).thenReturn("LoanBranch");
        when(loan.loanProduct()).thenReturn(product);
        when(product.getShortName()).thenReturn("LP01");

        AccountNumberFormat format = mock(AccountNumberFormat.class);
        when(format.getPrefixEnum()).thenReturn(null);

        String accountNumber = generator.generate(loan, format);
        assertThat(accountNumber).isEqualTo("000000077");
    }

    @Test
    public void testGenerateSavingsAccountNumber() {
        SavingsAccount savings = mock(SavingsAccount.class);
        Office office = mock(Office.class);
        SavingsProduct product = mock(SavingsProduct.class);

        when(savings.getId()).thenReturn(456L);
        when(savings.office()).thenReturn(office);
        when(office.getName()).thenReturn("Branch01");
        when(savings.savingsProduct()).thenReturn(product);
        when(product.getShortName()).thenReturn("SP01");

        AccountNumberFormat format = mock(AccountNumberFormat.class);
        when(format.getPrefixEnum()).thenReturn(null);

        String accountNumber = generator.generate(savings, format);
        assertThat(accountNumber).isEqualTo("000000456");
    }

    @Test
    public void testGenerateShareAccountNumber() {
        ShareAccount share = mock(ShareAccount.class);
        ShareProduct product = mock(ShareProduct.class);

        when(share.getId()).thenReturn(321L);
        when(share.getShareProduct()).thenReturn(product);
        when(product.getShortName()).thenReturn("SH01");

        AccountNumberFormat format = mock(AccountNumberFormat.class);
        when(format.getPrefixEnum()).thenReturn(null);

        String accountNumber = generator.generate(share, format);
        assertThat(accountNumber).isEqualTo("000000321");
    }

    @Test
    public void testCheckAccountNumberConflict_nullEntityType_returnsFalse() {
        Map<String, String> props = new HashMap<>();
        boolean result = generator.checkAccountNumberConflict(props, null, "12345");
        assertThat(result).isFalse();
    }

    @Test
    public void testCheckAccountNumberConflict_clientNoConflict() {
        Map<String, String> props = new HashMap<>();
        props.put("entityType", "client");
        when(clientRepo.getClientByAccountNumber("12345")).thenReturn(null);

        boolean result = generator.checkAccountNumberConflict(props, null, "12345");
        assertThat(result).isFalse();
    }

    @Test
    public void testCheckAccountNumberConflict_clientWithConflict() {
        Map<String, String> props = new HashMap<>();
        props.put("entityType", "client");
        when(clientRepo.getClientByAccountNumber("12345")).thenReturn(mock(Client.class));

        boolean result = generator.checkAccountNumberConflict(props, null, "12345");
        assertThat(result).isTrue();
    }

    @Test
    public void testCheckAccountNumberConflict_loanNoConflict() {
        Map<String, String> props = new HashMap<>();
        props.put("entityType", "loan");
        when(loanRepo.findLoanAccountByAccountNumber("77777")).thenReturn(null);

        boolean result = generator.checkAccountNumberConflict(props, null, "77777");
        assertThat(result).isFalse();
    }

    @Test
    public void testCheckAccountNumberConflict_loanWithConflict() {
        Map<String, String> props = new HashMap<>();
        props.put("entityType", "loan");
        when(loanRepo.findLoanAccountByAccountNumber("77777")).thenReturn(mock(Loan.class));

        boolean result = generator.checkAccountNumberConflict(props, null, "77777");
        assertThat(result).isTrue();
    }

    @Test
    public void testCheckAccountNumberConflict_savingsNoConflict() {
        Map<String, String> props = new HashMap<>();
        props.put("entityType", "savingsAccount");
        when(savingsRepo.findSavingsAccountByAccountNumber("55555")).thenReturn(null);

        boolean result = generator.checkAccountNumberConflict(props, null, "55555");
        assertThat(result).isFalse();
    }

    @Test
    public void testCheckAccountNumberConflict_savingsWithConflict() {
        Map<String, String> props = new HashMap<>();
        props.put("entityType", "savingsAccount");
        when(savingsRepo.findSavingsAccountByAccountNumber("55555")).thenReturn(mock(SavingsAccount.class));

        boolean result = generator.checkAccountNumberConflict(props, null, "55555");
        assertThat(result).isTrue();
    }

    @Test
    public void testCheckAccountNumberConflict_unknownEntityType_returnsFalse() {
        Map<String, String> props = new HashMap<>();
        props.put("entityType", "foobar");

        boolean result = generator.checkAccountNumberConflict(props, null, "12345");
        assertThat(result).isFalse();
    }
}
