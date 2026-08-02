package com.mapnaom.ticketingplatform.dto.access;

import java.time.LocalDateTime;
import java.util.Set;

public record AdminUserDto(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String username,
        String email,
        String avatarUrl,
        String userType,
        Set<String> roles,
        boolean deletable,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
