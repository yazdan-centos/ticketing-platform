package com.mapnaom.ticketingplatform.dto.meeting;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgendaItemRequest(
        @NotBlank @Size(max = 255) String topic,
        @Size(max = 4000) String description,
        @Min(0) Integer displayOrder,
        @Min(1) Integer durationMinutes,
        Long presenterId
) {
}
