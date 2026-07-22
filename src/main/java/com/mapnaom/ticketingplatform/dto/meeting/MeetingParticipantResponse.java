package com.mapnaom.ticketingplatform.dto.meeting;

import com.mapnaom.ticketingplatform.model.enums.RsvpStatus;

public record MeetingParticipantResponse(
        Long userId,
        String username,
        String fullName,
        RsvpStatus rsvpStatus,
        boolean attended
) {
}
