package com.mapnaom.ticketingplatform.dto.meeting;

import com.mapnaom.ticketingplatform.model.enums.MeetingStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MeetingResponse(
        Long id,
        String title,
        String description,
        Long teamId,
        String teamName,
        Long organizerId,
        String organizerName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String location,
        MeetingStatus status,
        List<AgendaItemResponse> agendaItems,
        List<MeetingParticipantResponse> participants,
        int participantCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
