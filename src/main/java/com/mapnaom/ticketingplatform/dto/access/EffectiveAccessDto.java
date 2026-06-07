package com.mapnaom.ticketingplatform.dto.access;

import com.mapnaom.ticketingplatform.model.AccessScope;

import java.util.Map;
import java.util.Set;

public record EffectiveAccessDto(
        Long userId,
        Set<String> roleNames,
        Set<String> permissionCodes,
        Map<String, AccessScope> scopes
) {
}
