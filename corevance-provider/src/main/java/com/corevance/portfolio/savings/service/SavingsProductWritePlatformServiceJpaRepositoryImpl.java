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
package com.corevance.portfolio.savings.service;

import static com.corevance.portfolio.savings.SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME;
import static com.corevance.portfolio.savings.SavingsApiConstants.accountingRuleParamName;
import static com.corevance.portfolio.savings.SavingsApiConstants.chargesParamName;
import static com.corevance.portfolio.savings.SavingsApiConstants.taxGroupIdParamName;

import jakarta.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.corevance.accounting.producttoaccountmapping.service.ProductToGLAccountMappingWritePlatformService;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.ApiParameterError;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.data.CommandProcessingResultBuilder;
import com.corevance.infrastructure.core.data.DataValidatorBuilder;
import com.corevance.infrastructure.core.exception.ErrorHandler;
import com.corevance.infrastructure.core.exception.PlatformApiDataValidationException;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityAccessType;
import com.corevance.infrastructure.entityaccess.service.CorevanceEntityAccessUtil;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.portfolio.charge.domain.Charge;
import com.corevance.portfolio.savings.DepositAccountType;
import com.corevance.portfolio.savings.SavingsApiConstants;
import com.corevance.portfolio.savings.data.SavingsProductDataValidator;
import com.corevance.portfolio.savings.domain.SavingsProduct;
import com.corevance.portfolio.savings.domain.SavingsProductAssembler;
import com.corevance.portfolio.savings.domain.SavingsProductRepository;
import com.corevance.portfolio.savings.exception.SavingsProductNotFoundException;
import com.corevance.portfolio.tax.domain.TaxGroup;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class SavingsProductWritePlatformServiceJpaRepositoryImpl implements SavingsProductWritePlatformService {

    private final PlatformSecurityContext context;
    private final SavingsProductRepository savingProductRepository;
    private final SavingsProductDataValidator fromApiJsonDataValidator;
    private final SavingsProductAssembler savingsProductAssembler;
    private final ProductToGLAccountMappingWritePlatformService accountMappingWritePlatformService;
    private final CorevanceEntityAccessUtil corevanceEntityAccessUtil;

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dae) {
        String msgCode = "error.msg." + SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME;
        String msg = "Unknown data integrity issue with savings product.";
        String param = null;
        Object[] msgArgs;
        Throwable checkEx = realCause == null ? dae : realCause;
        if (checkEx.getMessage().contains("sp_unq_name")) {
            final String name = command.stringValueOfParameterNamed("name");
            msgCode += ".duplicate.name";
            msg = "Savings product with name `" + name + "` already exists";
            param = "name";
            msgArgs = new Object[] { name, dae };
        } else if (checkEx.getMessage().contains("sp_unq_short_name")) {
            final String shortName = command.stringValueOfParameterNamed("shortName");
            msgCode += ".duplicate.short.name";
            msg = "Savings product with short name `" + shortName + "` already exists";
            param = "shortName";
            msgArgs = new Object[] { shortName, dae };
        } else {
            msgCode += ".unknown.data.integrity.issue";
            msgArgs = new Object[] { dae };
        }
        log.error("Error occured.", dae);
        throw ErrorHandler.getMappable(dae, msgCode, msg, param, msgArgs);
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {

        try {
            this.fromApiJsonDataValidator.validateForCreate(command.json());

            final SavingsProduct product = this.savingsProductAssembler.assemble(command);

            this.savingProductRepository.saveAndFlush(product);

            // save accounting mappings
            this.accountMappingWritePlatformService.createSavingProductToGLAccountMapping(product.getId(), command,
                    DepositAccountType.SAVINGS_DEPOSIT);

            // check if the office specific products are enabled. If yes, then
            // save this savings product against a specific office
            // i.e. this savings product is specific for this office.
            corevanceEntityAccessUtil.checkConfigurationAndAddProductResrictionsForUserOffice(
                    CorevanceEntityAccessType.OFFICE_ACCESS_TO_SAVINGS_PRODUCTS, product.getId());

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e.getMostSpecificCause(), e);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            handleDataIntegrityIssues(command, ExceptionUtils.getRootCause(dve.getCause()), dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult update(final Long productId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            final SavingsProduct product = this.savingProductRepository.findById(productId)
                    .orElseThrow(() -> new SavingsProductNotFoundException(productId));

            this.fromApiJsonDataValidator.validateForUpdate(command.json(), product);

            final Map<String, Object> changes = product.update(command);

            if (changes.containsKey(chargesParamName)) {
                final Set<Charge> savingsProductCharges = this.savingsProductAssembler.assembleListOfSavingsProductCharges(command,
                        product.currency().getCode());
                final boolean updated = product.update(savingsProductCharges);
                if (!updated) {
                    changes.remove(chargesParamName);
                }
            }

            if (changes.containsKey(taxGroupIdParamName)) {
                final TaxGroup taxGroup = this.savingsProductAssembler.assembleTaxGroup(command);
                product.setTaxGroup(taxGroup);
                if (product.withHoldTax() && product.getTaxGroup() == null) {
                    final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                    final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                            .resource(SAVINGS_PRODUCT_RESOURCE_NAME);
                    final Long taxGroupId = null;
                    baseDataValidator.reset().parameter(taxGroupIdParamName).value(taxGroupId).notBlank();
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }

            // accounting related changes
            final boolean accountingTypeChanged = changes.containsKey(accountingRuleParamName);
            final Map<String, Object> accountingMappingChanges = this.accountMappingWritePlatformService
                    .updateSavingsProductToGLAccountMapping(product.getId(), command, accountingTypeChanged, product.getAccountingType(),
                            DepositAccountType.SAVINGS_DEPOSIT);
            changes.putAll(accountingMappingChanges);

            if (!changes.isEmpty()) {
                this.savingProductRepository.saveAndFlush(product);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(product.getId()) //
                    .with(changes) //
                    .build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e.getMostSpecificCause(), e);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            handleDataIntegrityIssues(command, ExceptionUtils.getRootCause(dve.getCause()), dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long productId) {

        this.context.authenticatedUser();
        final SavingsProduct product = this.savingProductRepository.findById(productId)
                .orElseThrow(() -> new SavingsProductNotFoundException(productId));

        this.savingProductRepository.delete(product);

        return new CommandProcessingResultBuilder() //
                .withEntityId(product.getId()) //
                .build();
    }

}
