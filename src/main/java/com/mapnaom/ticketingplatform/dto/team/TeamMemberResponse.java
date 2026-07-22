package com.mapnaom.ticketingplatform.dto.team;

import com.mapnaom.ticketingplatform.model.enums.TeamRole;

import java.time.LocalDateTime;

public record TeamMemberResponse(
        Long userId,
        String username,
        String fullName,
        TeamRole role,
        LocalDateTime joinedAt
) {
}
