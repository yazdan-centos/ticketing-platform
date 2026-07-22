package com.mapnaom.ticketingplatform.dto.meeting;

import com.mapnaom.ticketingplatform.model.enums.MeetingStatus;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record MeetingUpdateRequest(
        @Size(min = 1, max = 255) String title,
        @Size(max = 10000) String description,
        LocalDateTime startTime,
        LocalDateTime endTime,
        @Size(max = 1000) String location,
        MeetingStatus status
) {
}
