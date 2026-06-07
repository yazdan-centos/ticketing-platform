package com.mapnaom.ticketingplatform.dto.access;

import com.mapnaom.ticketingplatform.model.AccessScope;

public record ScopeDto(String resourceType, AccessScope scope) {
}
