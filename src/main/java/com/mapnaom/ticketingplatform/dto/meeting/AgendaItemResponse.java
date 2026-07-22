package com.mapnaom.ticketingplatform.dto.meeting;

public record AgendaItemResponse(
        Long id,
        String topic,
        String description,
        Integer displayOrder,
        Integer durationMinutes,
        Long presenterId,
        String presenterName
) {
}
