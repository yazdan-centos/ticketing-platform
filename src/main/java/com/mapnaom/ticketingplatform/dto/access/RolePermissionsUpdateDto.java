package com.mapnaom.ticketingplatform.dto.access;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RolePermissionsUpdateDto(
        @NotNull List<@NotBlank String> permissionCodes
) {
}
