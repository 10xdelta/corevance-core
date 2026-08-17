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
package com.corevance.commands.service;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.commands.domain.CommandSource;
import com.corevance.commands.domain.CommandSourceRepository;
import com.corevance.commands.domain.CommandWrapper;
import com.corevance.commands.exception.CommandNotAwaitingApprovalException;
import com.corevance.commands.exception.CommandNotFoundException;
import com.corevance.commands.exception.UnsupportedCommandException;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.api.JsonCommand;
import com.corevance.infrastructure.core.data.CommandProcessingResult;
import com.corevance.infrastructure.core.serialization.FromJsonHelper;
import com.corevance.infrastructure.dataqueries.service.CleanupService;
import com.corevance.infrastructure.jobs.service.SchedulerJobRunnerReadService;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioCommandSourceWritePlatformServiceImpl implements PortfolioCommandSourceWritePlatformService {

    private final PlatformSecurityContext context;
    private final CommandSourceRepository commandSourceRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final CommandProcessingService processAndLogCommandService;
    private final SchedulerJobRunnerReadService schedulerJobRunnerReadService;
    private final ConfigurationDomainService configurationService;
    private final List<CleanupService> cleanupServices;

    @Override
    public CommandProcessingResult logCommandSource(final CommandWrapper wrapper) {
        boolean isApprovedByChecker = false;

        // check if is update of own account details
        if (wrapper.isChangeOfOwnUserDetails(this.context.authenticatedUser(wrapper).getId())) {
            // then allow this operation to proceed.
            // maker checker doesnt mean anything here.
            isApprovedByChecker = true; // set to true in case permissions have
                                        // been maker-checker enabled by
                                        // accident.
        } else {
            // if not user changing their own details - check user has
            // permission to perform specific task.
            this.context.authenticatedUser(wrapper).validateHasPermissionTo(wrapper.getTaskPermissionName());
        }
        validateIsUpdateAllowed();

        final String json = wrapper.getJson();
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(json);
        JsonCommand command = JsonCommand.from(json, parsedCommand, this.fromApiJsonHelper, wrapper.getEntityName(), wrapper.getEntityId(),
                wrapper.getSubentityId(), wrapper.getGroupId(), wrapper.getClientId(), wrapper.getLoanId(), wrapper.getSavingsId(),
                wrapper.getTransactionId(), wrapper.getHref(), wrapper.getProductId(), wrapper.getCreditBureauId(),
                wrapper.getOrganisationCreditBureauId(), wrapper.getJobName(), wrapper.getLoanExternalId());

        return this.processAndLogCommandService.executeCommand(wrapper, command, isApprovedByChecker);
    }

    @Override
    public CommandProcessingResult approveEntry(final Long makerCheckerId) {
        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();

        final CommandWrapper wrapper = CommandWrapper.fromExistingCommand(makerCheckerId, commandSourceInput.getActionName(),
                commandSourceInput.getEntityName(), commandSourceInput.getResourceId(), commandSourceInput.getSubResourceId(),
                commandSourceInput.getResourceGetUrl(), commandSourceInput.getProductId(), commandSourceInput.getOfficeId(),
                commandSourceInput.getGroupId(), commandSourceInput.getClientId(), commandSourceInput.getLoanId(),
                commandSourceInput.getSavingsId(), commandSourceInput.getTransactionId(), commandSourceInput.getCreditBureauId(),
                commandSourceInput.getOrganisationCreditBureauId(), commandSourceInput.getIdempotencyKey(),
                commandSourceInput.getLoanExternalId());
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(commandSourceInput.getCommandAsJson());
        final JsonCommand command = JsonCommand.fromExistingCommand(makerCheckerId, commandSourceInput.getCommandAsJson(), parsedCommand,
                this.fromApiJsonHelper, commandSourceInput.getEntityName(), commandSourceInput.getResourceId(),
                commandSourceInput.getSubResourceId(), commandSourceInput.getGroupId(), commandSourceInput.getClientId(),
                commandSourceInput.getLoanId(), commandSourceInput.getSavingsId(), commandSourceInput.getTransactionId(),
                commandSourceInput.getResourceGetUrl(), commandSourceInput.getProductId(), commandSourceInput.getCreditBureauId(),
                commandSourceInput.getOrganisationCreditBureauId(), commandSourceInput.getJobName(),
                commandSourceInput.getLoanExternalId());

        return this.processAndLogCommandService.executeCommand(wrapper, command, true);
    }

    @Transactional
    @Override
    public Long deleteEntry(final Long makerCheckerId) {

        validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();

        this.commandSourceRepository.deleteById(makerCheckerId);

        return makerCheckerId;
    }

    private CommandSource validateMakerCheckerTransaction(final Long makerCheckerId) {
        final CommandSource commandSource = this.commandSourceRepository.findById(makerCheckerId)
                .orElseThrow(() -> new CommandNotFoundException(makerCheckerId));
        if (!commandSource.isAwaitingApproval()) {
            throw new CommandNotAwaitingApprovalException(makerCheckerId);
        }
        AppUser appUser = this.context.authenticatedUser();
        String permissionCode = commandSource.getPermissionCode();
        appUser.validateHasCheckerPermissionTo(permissionCode);
        if (!configurationService.isSameMakerCheckerEnabled() && !appUser.isCheckerSuperUser()) {
            AppUser maker = commandSource.getMaker();
            if (maker == null) {
                throw new UnsupportedCommandException(permissionCode, "Maker user is missing.");
            }
            if (Objects.equals(appUser.getId(), maker.getId())) {
                throw new UnsupportedCommandException(permissionCode, "Can not be checked by the same user.");
            }
        }
        return commandSource;
    }

    private void validateIsUpdateAllowed() {
        this.schedulerJobRunnerReadService.isUpdatesAllowed();
    }

    @Override
    public Long rejectEntry(final Long makerCheckerId) {
        final CommandSource commandSourceInput = validateMakerCheckerTransaction(makerCheckerId);
        validateIsUpdateAllowed();
        final AppUser maker = this.context.authenticatedUser();
        commandSourceInput.markAsRejected(maker);
        this.commandSourceRepository.save(commandSourceInput);
        if (cleanupServices != null) {
            for (CleanupService cleanupService : cleanupServices) {
                cleanupService.cleanup(commandSourceInput);
            }
        }
        return makerCheckerId;
    }
}
