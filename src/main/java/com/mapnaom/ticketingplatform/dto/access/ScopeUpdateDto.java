package com.mapnaom.ticketingplatform.dto.access;

import com.mapnaom.ticketingplatform.model.AccessScope;
import jakarta.validation.constraints.NotNull;

public record ScopeUpdateDto(@NotNull AccessScope scope) {
}
