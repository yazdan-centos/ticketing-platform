package com.mapnaom.ticketingplatform.dto.team;

import java.time.LocalDateTime;
import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        List<TeamMemberResponse> members
) {
}
