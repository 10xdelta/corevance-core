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
package com.corevance.portfolio.meeting.service;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.corevance.infrastructure.configuration.domain.ConfigurationDomainService;
import com.corevance.infrastructure.core.exception.ErrorHandler;
import com.corevance.infrastructure.core.exception.PlatformDataIntegrityException;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.portfolio.calendar.domain.CalendarEntityType;
import com.corevance.portfolio.calendar.domain.CalendarInstance;
import com.corevance.portfolio.calendar.domain.CalendarInstanceRepository;
import com.corevance.portfolio.calendar.domain.CalendarRepository;
import com.corevance.portfolio.calendar.exception.CalendarInstanceNotFoundException;
import com.corevance.portfolio.calendar.exception.CalendarNotFoundException;
import com.corevance.portfolio.calendar.exception.NotValidRecurringDateException;
import com.corevance.portfolio.client.domain.ClientRepository;
import com.corevance.portfolio.client.exception.ClientNotFoundException;
import com.corevance.portfolio.group.domain.Group;
import com.corevance.portfolio.group.domain.GroupRepository;
import com.corevance.portfolio.group.exception.ClientNotInGroupException;
import com.corevance.portfolio.meeting.data.MeetingAttendanceData;
import com.corevance.portfolio.meeting.data.MeetingCreateRequest;
import com.corevance.portfolio.meeting.data.MeetingCreateResponse;
import com.corevance.portfolio.meeting.data.MeetingDeleteRequest;
import com.corevance.portfolio.meeting.data.MeetingDeleteResponse;
import com.corevance.portfolio.meeting.data.MeetingUpdateRequest;
import com.corevance.portfolio.meeting.data.MeetingUpdateResponse;
import com.corevance.portfolio.meeting.domain.Meeting;
import com.corevance.portfolio.meeting.domain.MeetingAttendance;
import com.corevance.portfolio.meeting.domain.MeetingRepository;
import com.corevance.portfolio.meeting.exception.MeetingDateException;
import com.corevance.portfolio.meeting.exception.MeetingNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnMissingBean(value = MeetingWriteService.class, ignored = MeetingWriteServiceImpl.class)
public class MeetingWriteServiceImpl implements MeetingWriteService {

    private final MeetingRepository meetingRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final CalendarRepository calendarRepository;
    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Override
    public MeetingCreateResponse createMeeting(@Valid final MeetingCreateRequest request) {
        var isTransactionDateOnNonMeetingDate = false;

        var meetingDate = LocalDate.parse(request.getMeetingDate(),
                DateTimeFormatter.ofPattern(request.getDateFormat(), Locale.of(request.getLocale())));

        try {
            var calendarInstance = getCalendarInstance(request.getCalendarId(), request.getEntityId(), request.getEntityType());
            var isSkipRepaymentOnFirstMonth = false;
            var numberOfDays = 0;
            var isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();

            if (isSkipRepaymentOnFirstMonthEnabled) {
                isSkipRepaymentOnFirstMonth = true;
                numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
            }

            var meeting = createNew(calendarInstance, meetingDate, isTransactionDateOnNonMeetingDate, isSkipRepaymentOnFirstMonth,
                    numberOfDays);

            // do not allow to capture attendance in advance.
            if (DateUtils.isAfter(meeting.getMeetingDate(), DateUtils.getBusinessLocalDate())) {
                throw new MeetingDateException("cannot.be.a.future.date", "Attendance cannot be in the future.", meeting.getMeetingDate());
            }
            meeting.setClientsAttendance(new HashSet<>(getClientsAttendance(meeting, request.getClientsAttendance())));

            meetingRepository.saveAndFlush(meeting);

            var groupId = CalendarEntityType.isGroup(meeting.getCalendarInstance().getEntityTypeId())
                    ? meeting.getCalendarInstance().getEntityId()
                    : null;

            return MeetingCreateResponse.builder().entityId(meeting.getId()).groupId(groupId).build();

        } catch (final DataIntegrityViolationException | JpaSystemException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            throw handleMeetingDataIntegrityIssues(meetingDate, throwable, dve);
        }
    }

    @Override
    public MeetingUpdateResponse updateMeeting(@Valid final MeetingUpdateRequest request) {
        var isSkipRepaymentOnFirstMonth = false;
        var numberOfDays = 0;
        boolean isSkipRepaymentOnFirstMonthEnabled = configurationDomainService.isSkippingMeetingOnFirstDayOfMonthEnabled();

        if (isSkipRepaymentOnFirstMonthEnabled) {
            isSkipRepaymentOnFirstMonth = true;
            numberOfDays = configurationDomainService.retreivePeriodInNumberOfDaysForSkipMeetingDate().intValue();
        }

        var meeting = meetingRepository.findById(request.getId()).orElseThrow(() -> new MeetingNotFoundException(request.getId()));

        var meetingDate = LocalDate.parse(request.getMeetingDate(),
                DateTimeFormatter.ofPattern(request.getDateFormat(), Locale.of(request.getLocale())));

        if (!meetingDate.equals(meeting.getMeetingDate())) {
            meeting.setMeetingDate(meetingDate);

            if (meetingDate == null || !meeting.getCalendarInstance().getCalendar().isValidRecurringDate(meetingDate,
                    isSkipRepaymentOnFirstMonth, numberOfDays)) {
                throw new NotValidRecurringDateException("meeting", "Not a valid meeting date", meeting.getMeetingDate());
            }

            try {
                meetingRepository.saveAndFlush(meeting);
            } catch (final DataIntegrityViolationException | JpaSystemException dve) {
                final Throwable throwable = dve.getMostSpecificCause();
                throw handleMeetingDataIntegrityIssues(meeting.getMeetingDate(), throwable, dve);
            }
        }

        var groupId = CalendarEntityType.isGroup(meeting.getCalendarInstance().getEntityTypeId())
                ? meeting.getCalendarInstance().getEntityId()
                : null;

        return MeetingUpdateResponse.builder().entityId(meeting.getId()).groupId(groupId).build();
    }

    @Override
    public MeetingDeleteResponse deleteMeeting(@Valid final MeetingDeleteRequest request) {
        var meeting = meetingRepository.findById(request.getId()).orElseThrow(() -> new MeetingNotFoundException(request.getId()));

        meetingRepository.delete(meeting);

        return MeetingDeleteResponse.builder().entityId(request.getId()).build();
    }

    private Meeting createNew(CalendarInstance calendarInstance, LocalDate meetingDate, Boolean isTransactionDateOnNonMeetingDate,
            boolean isSkipRepaymentOnFirstMonth, int numberOfDays) {

        if (!isTransactionDateOnNonMeetingDate && (meetingDate == null
                || !calendarInstance.getCalendar().isValidRecurringDate(meetingDate, isSkipRepaymentOnFirstMonth, numberOfDays))) {
            throw new NotValidRecurringDateException("meeting", "The date '" + meetingDate + "' is not a valid meeting date.", meetingDate);
        }

        return new Meeting(calendarInstance, meetingDate, Set.of());
    }

    private CalendarInstance getCalendarInstance(Long calendarId, Long entityId, CalendarEntityType entityType) {
        var calendar = this.calendarRepository.findById(calendarId).orElseThrow(() -> new CalendarNotFoundException(calendarId));

        if (CalendarEntityType.GROUPS.equals(entityType)) {
            final Group group = this.groupRepository.findById(entityId).orElseThrow();

            if (group.isCenter()) {
                entityType = CalendarEntityType.CENTERS;
            } else if (group.isChildGroup()) {
                entityType = CalendarEntityType.CENTERS;
                entityId = group.getParent().getId();
            }
        }

        final var calendarInstance = this.calendarInstanceRepository.findByCalendarIdAndEntityIdAndEntityTypeId(calendar.getId(), entityId,
                entityType.getValue());

        if (calendarInstance == null) {
            throw new CalendarInstanceNotFoundException("for." + entityType.name().toLowerCase() + "not.found",
                    "No Calendar Instance details found for group with identifier " + entityId + " and calendar with identifier "
                            + calendarId,
                    entityId, calendarId);
        }

        return calendarInstance;
    }

    HashSet<MeetingAttendance> getClientsAttendance(Meeting meeting, List<MeetingAttendanceData> attendances) {
        var meetingAttendances = new HashSet<MeetingAttendance>();

        for (var attendance : attendances) {
            var client = clientRepository.findById(attendance.getClientId())
                    .orElseThrow(() -> new ClientNotFoundException(attendance.getClientId()));

            if (CalendarEntityType.isGroup(meeting.getCalendarInstance().getEntityTypeId())
                    && !client.isChildOfGroup(meeting.getCalendarInstance().getEntityId())) {
                throw new ClientNotInGroupException(attendance.getId(), meeting.getCalendarInstance().getEntityId());
            } else if (CalendarEntityType.isCenter(meeting.getCalendarInstance().getEntityTypeId())) {
                if (CalendarEntityType.isCenter(meeting.getCalendarInstance().getEntityTypeId())) {
                    var size = groupRepository.findByParentId(meeting.getCalendarInstance().getEntityId()).stream()
                            .filter(group -> group.isChildClient(attendance.getId())).count();

                    if (size == 0L) {
                        throw new ClientNotInGroupException("client.not.in.center",
                                "Client with identifier " + attendance.getId() + " is not in center "
                                        + meeting.getCalendarInstance().getEntityId(),
                                attendance.getId(), meeting.getCalendarInstance().getEntityId());
                    }
                }
            }

            meetingAttendances.add(new MeetingAttendance(client, meeting, attendance.getAttendanceType().getId().intValue()));
        }

        return meetingAttendances;
    }

    private RuntimeException handleMeetingDataIntegrityIssues(final LocalDate meetingDate, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("unique_calendar_instance_id_meeting_date")) {
            throw new PlatformDataIntegrityException("error.msg.meeting.duplicate",
                    "A meeting with date '" + meetingDate + "' already exists", "meetingDate", meetingDate);
        }

        return ErrorHandler.getMappable(dve, "error.msg.meeting.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
