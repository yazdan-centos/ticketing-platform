package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.meeting.AgendaItemRequest;
import com.mapnaom.ticketingplatform.dto.meeting.AgendaItemResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingCreateRequest;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingNoteRequest;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingNoteResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingUpdateRequest;
import com.mapnaom.ticketingplatform.model.enums.RsvpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingService {
    MeetingResponse createMeeting(MeetingCreateRequest request);

    MeetingResponse getMeeting(Long meetingId);

    Page<MeetingResponse> getMeetingsForTeam(Long teamId, Pageable pageable);

    List<MeetingResponse> getUpcomingMeetingsForUser(Long userId, LocalDateTime from, LocalDateTime to);

    MeetingResponse updateMeeting(Long meetingId, MeetingUpdateRequest request);

    void cancelMeeting(Long meetingId);

    void deleteMeeting(Long meetingId);

    void addParticipants(Long meetingId, List<Long> userIds);

    void respondToInvite(Long meetingId, Long userId, RsvpStatus response);

    void markAttendance(Long meetingId, Long userId, boolean attended);

    AgendaItemResponse addAgendaItem(Long meetingId, AgendaItemRequest request);

    void reorderAgenda(Long meetingId, List<Long> orderedAgendaItemIds);

    MeetingNoteResponse addNote(Long meetingId, MeetingNoteRequest request);

    List<MeetingNoteResponse> getNotes(Long meetingId);
}
