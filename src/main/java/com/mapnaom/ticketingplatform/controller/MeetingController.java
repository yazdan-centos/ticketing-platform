package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.ApiResponse;
import com.mapnaom.ticketingplatform.dto.meeting.AgendaItemRequest;
import com.mapnaom.ticketingplatform.dto.meeting.AgendaItemResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingCreateRequest;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingNoteRequest;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingNoteResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingResponse;
import com.mapnaom.ticketingplatform.dto.meeting.MeetingUpdateRequest;
import com.mapnaom.ticketingplatform.model.enums.RsvpStatus;
import com.mapnaom.ticketingplatform.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService meetingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<MeetingResponse>> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(meetingService.createMeeting(request), "Meeting created"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MeetingResponse>> getMeeting(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getMeeting(id)));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<Page<MeetingResponse>>> getTeamMeetings(
            @PathVariable Long teamId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getMeetingsForTeam(teamId, pageable)));
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getUpcomingForUser(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(ApiResponse.success(
                meetingService.getUpcomingMeetingsForUser(userId, from, to)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<MeetingResponse>> updateMeeting(
            @PathVariable Long id,
            @Valid @RequestBody MeetingUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.updateMeeting(id, request), "Meeting updated"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> cancelMeeting(@PathVariable Long id) {
        meetingService.cancelMeeting(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Meeting cancelled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteMeeting(@PathVariable Long id) {
        meetingService.deleteMeeting(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Meeting deleted"));
    }

    @PostMapping("/{id}/participants")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> addParticipants(
            @PathVariable Long id,
            @RequestBody List<Long> userIds) {
        meetingService.addParticipants(id, userIds);
        return ResponseEntity.ok(ApiResponse.success(null, "Participants added"));
    }

    @PostMapping("/{id}/participants/{userId}/rsvp")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> respondToInvite(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam RsvpStatus response) {
        meetingService.respondToInvite(id, userId, response);
        return ResponseEntity.ok(ApiResponse.success(null, "RSVP updated"));
    }

    @PutMapping("/{id}/participants/{userId}/attendance")
    @PreAuthorize("hasRole('TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> markAttendance(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestParam boolean attended) {
        meetingService.markAttendance(id, userId, attended);
        return ResponseEntity.ok(ApiResponse.success(null, "Attendance updated"));
    }

    @PostMapping("/{id}/agenda")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<AgendaItemResponse>> addAgendaItem(
            @PathVariable Long id,
            @Valid @RequestBody AgendaItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(meetingService.addAgendaItem(id, request), "Agenda item added"));
    }

    @PutMapping("/{id}/agenda/reorder")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> reorderAgenda(
            @PathVariable Long id,
            @RequestBody List<Long> orderedIds) {
        meetingService.reorderAgenda(id, orderedIds);
        return ResponseEntity.ok(ApiResponse.success(null, "Agenda reordered"));
    }

    @PostMapping("/{id}/notes")
    @PreAuthorize("hasAnyRole('TEAM_MEMBER', 'TEAM_MANAGER')")
    public ResponseEntity<ApiResponse<MeetingNoteResponse>> addNote(
            @PathVariable Long id,
            @Valid @RequestBody MeetingNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(meetingService.addNote(id, request), "Meeting note added"));
    }

    @GetMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<List<MeetingNoteResponse>>> getNotes(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getNotes(id)));
    }
}
