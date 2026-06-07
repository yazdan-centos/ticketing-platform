package com.mapnaom.ticketingplatform.dto.access;

import com.mapnaom.ticketingplatform.model.enums.GrantEffect;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GrantDto(
        @NotBlank String permissionCode,
        @NotNull GrantEffect effect
) {
}
