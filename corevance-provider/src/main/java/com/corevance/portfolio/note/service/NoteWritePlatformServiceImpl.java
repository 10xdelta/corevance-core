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
package com.corevance.portfolio.note.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import com.corevance.portfolio.client.domain.ClientRepositoryWrapper;
import com.corevance.portfolio.group.domain.GroupRepository;
import com.corevance.portfolio.group.exception.GroupNotFoundException;
import com.corevance.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import com.corevance.portfolio.loanaccount.domain.LoanTransactionRepository;
import com.corevance.portfolio.loanaccount.exception.LoanTransactionNotFoundException;
import com.corevance.portfolio.note.data.NoteCreateRequest;
import com.corevance.portfolio.note.data.NoteCreateResponse;
import com.corevance.portfolio.note.data.NoteDeleteRequest;
import com.corevance.portfolio.note.data.NoteDeleteResponse;
import com.corevance.portfolio.note.data.NoteUpdateRequest;
import com.corevance.portfolio.note.data.NoteUpdateResponse;
import com.corevance.portfolio.note.domain.Note;
import com.corevance.portfolio.note.domain.NoteRepository;
import com.corevance.portfolio.note.domain.NoteType;
import com.corevance.portfolio.note.exception.NoteNotFoundException;
import com.corevance.portfolio.note.exception.NoteResourceNotSupportedException;
import com.corevance.portfolio.savings.domain.SavingsAccountRepository;
import com.corevance.portfolio.savings.domain.SavingsAccountTransactionRepository;
import com.corevance.portfolio.savings.exception.SavingsAccountNotFoundException;
import com.corevance.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import com.corevance.portfolio.shareaccounts.domain.ShareAccountRepositoryWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = NoteWritePlatformService.class, ignored = NoteWritePlatformServiceImpl.class)
public class NoteWritePlatformServiceImpl implements NoteWritePlatformService {

    private final NoteRepository noteRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final SavingsAccountTransactionRepository savingsAccountTransactionRepository;
    private final ShareAccountRepositoryWrapper shareAccountRepository;

    @Override
    public NoteCreateResponse createNote(final NoteCreateRequest request) {
        Note note;
        Long officeId;

        switch (request.getType()) {
            case CLIENT: {
                final var client = this.clientRepository.findOneWithNotFoundDetection(request.getResourceId());
                note = noteRepository.saveAndFlush(Note.clientNote(client, request.getNote()));
                officeId = client.officeId();
            }
            break;
            case GROUP: {
                final var group = groupRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new GroupNotFoundException(request.getResourceId()));
                note = noteRepository.saveAndFlush(Note.groupNote(group, request.getNote()));
                officeId = group.officeId();
            }
            break;
            case LOAN: {
                final var loan = loanRepository.findOneWithNotFoundDetection(request.getResourceId());
                note = noteRepository.saveAndFlush(Note.loanNote(loan, request.getNote()));
                officeId = loan.getOfficeId();
            }
            break;
            case LOAN_TRANSACTION: {
                final var loanTransaction = this.loanTransactionRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new LoanTransactionNotFoundException(request.getResourceId()));
                note = noteRepository.saveAndFlush(Note.loanTransactionNote(loanTransaction.getLoan(), loanTransaction, request.getNote()));
                officeId = loanTransaction.getLoan().getOfficeId();
            }
            break;
            case SAVING_ACCOUNT: {
                final var savingAccount = savingsAccountRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new SavingsAccountNotFoundException(request.getResourceId()));
                note = noteRepository.saveAndFlush(Note.savingNote(savingAccount, request.getNote()));
                officeId = savingAccount.getClient().getOffice().getId();
            }
            break;
            case SAVINGS_TRANSACTION: {
                final var savingsTransaction = savingsAccountTransactionRepository.findById(request.getResourceId())
                        .orElseThrow(() -> new SavingsAccountTransactionNotFoundException(null, request.getResourceId()));
                final var savingsAccount = savingsTransaction.getSavingsAccount();
                note = noteRepository.saveAndFlush(Note.savingsTransactionNote(savingsAccount, savingsTransaction, request.getNote()));
                officeId = savingsAccount.getClient().getOffice().getId();
            }
            break;
            case SHARE_ACCOUNT: {
                final var shareAccount = shareAccountRepository.findOneWithNotFoundDetection(request.getResourceId());
                note = noteRepository.saveAndFlush(Note.shareNote(shareAccount, request.getNote()));
                officeId = shareAccount.getOfficeId();
            }
            break;
            default:
                throw new NoteResourceNotSupportedException(request.getType().getApiUrl());
        }

        return NoteCreateResponse.builder().entityId(note.getId()).resourceId(note.getId()).officeId(officeId).build();
    }

    @Override
    public NoteUpdateResponse updateNote(final NoteUpdateRequest request) {
        final var result = getNote(request.getType(), request.getResourceId(), request.getId());
        final var note = result.getLeft();
        final var response = NoteUpdateResponse.builder().officeId(result.getRight()).resourceId(request.getResourceId());

        if (!Strings.CI.equals(note.getNote(), request.getNote())) {
            response.changes(note.update(request.getNote()));
            noteRepository.saveAndFlush(note);
        }

        return response.build();
    }

    @Override
    public NoteDeleteResponse deleteNote(final NoteDeleteRequest request) {
        var note = getNote(request.getType(), request.getResourceId(), request.getId());

        noteRepository.delete(note.getLeft());

        return NoteDeleteResponse.builder().resourceId(request.getId()).build();
    }

    private Pair<Note, Long> getNote(NoteType type, Long resourceId, Long noteId) {
        Note note = null;
        Long officeId = null;

        switch (type) {
            case CLIENT: {
                final var client = clientRepository.findOneWithNotFoundDetection(resourceId);
                note = noteRepository.findByClientAndId(client, noteId);
                officeId = client.officeId();
            }
            break;
            case GROUP: {
                final var group = groupRepository.findById(resourceId).orElseThrow(() -> new GroupNotFoundException(resourceId));
                note = noteRepository.findByGroupAndId(group, noteId);
                officeId = group.officeId();
            }
            break;
            case LOAN: {
                final var loan = loanRepository.findOneWithNotFoundDetection(resourceId);
                note = noteRepository.findByLoanAndId(loan, noteId);
                officeId = loan.getOfficeId();
            }
            break;
            case LOAN_TRANSACTION: {
                final var loanTransaction = loanTransactionRepository.findById(resourceId)
                        .orElseThrow(() -> new LoanTransactionNotFoundException(resourceId));
                note = noteRepository.findByLoanTransactionAndId(loanTransaction, noteId);
                officeId = loanTransaction.getLoan().getOfficeId();
            }
            break;
            case SAVING_ACCOUNT: {
                final var savingAccount = savingsAccountRepository.findById(resourceId)
                        .orElseThrow(() -> new SavingsAccountNotFoundException(resourceId));
                note = noteRepository.findBySavingsAccountAndId(savingAccount, noteId);
                officeId = savingAccount.getClient().getOffice().getId();
            }
            break;
            case SAVINGS_TRANSACTION: {
                final var savingsTransaction = savingsAccountTransactionRepository.findById(resourceId)
                        .orElseThrow(() -> new SavingsAccountTransactionNotFoundException(null, resourceId));
                note = noteRepository.findBySavingsTransactionAndId(savingsTransaction, noteId);
                officeId = savingsTransaction.getSavingsAccount().getClient().getOffice().getId();
            }
            break;
            case SHARE_ACCOUNT: {
                final var shareAccount = shareAccountRepository.findOneWithNotFoundDetection(resourceId);
                note = noteRepository.findByShareAccountAndId(shareAccount, noteId);
                officeId = shareAccount.getOfficeId();
            }
            break;
            default:
                log.error("Not yet implemented: {}", type);
            break;
        }

        if (note == null) {
            throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase());
        }

        return Pair.of(note, officeId);
    }
}
