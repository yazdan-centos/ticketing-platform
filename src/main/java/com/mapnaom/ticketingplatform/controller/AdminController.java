package com.mapnaom.ticketingplatform.controller;


import com.mapnaom.ticketingplatform.dto.access.EffectiveAccessDto;
import com.mapnaom.ticketingplatform.dto.access.GrantDto;
import com.mapnaom.ticketingplatform.dto.access.PermissionDto;
import com.mapnaom.ticketingplatform.dto.access.RolePermissionsUpdateDto;
import com.mapnaom.ticketingplatform.dto.access.ScopeDto;
import com.mapnaom.ticketingplatform.dto.access.ScopeUpdateDto;
import com.mapnaom.ticketingplatform.service.AccessAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Administrative access-control operations.
 *
 * Every endpoint is gated by the ACCESS_ADMIN authority. That code is part of
 * the fixed permission catalog and is seeded onto the TEAM_MANAGER role, but
 * because authorities are resolved from the database per request, an admin can
 * also grant or revoke it on any individual user without a redeploy.
 */
@RestController
@RequestMapping("/api/admin/access")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ACCESS_ADMIN')")
public class AdminController {

    private final AccessAdminService accessAdminService;

    // --- Permission catalog (read-only; codes are defined in code) -----------

    @GetMapping("/permissions")
    public List<PermissionDto> listPermissions() {
        return accessAdminService.listPermissions();
    }

    // --- Effective access for a user (resolved view for the UI) --------------

    @GetMapping("/users/{userId}")
    public EffectiveAccessDto getEffectiveAccess(@PathVariable Long userId) {
        return accessAdminService.getEffectiveAccess(userId);
    }

    // --- Per-user permission grants (ALLOW adds, DENY strips) -----------------

    @GetMapping("/users/{userId}/grants")
    public List<GrantDto> listGrants(@PathVariable Long userId) {
        return accessAdminService.listGrants(userId);
    }

    @PostMapping("/users/{userId}/grants")
    @ResponseStatus(HttpStatus.CREATED)
    public GrantDto upsertGrant(@PathVariable Long userId,
                                @RequestBody @Valid GrantDto dto) {
        // ALLOW or DENY for a single permission code; replaces any existing
        // grant for that (user, permission) pair.
        return accessAdminService.upsertGrant(userId, dto.permissionCode(), dto.effect());
    }

    @DeleteMapping("/users/{userId}/grants/{permissionCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeGrant(@PathVariable Long userId,
                            @PathVariable String permissionCode) {
        // Removing an override returns the user to their role default.
        accessAdminService.removeGrant(userId, permissionCode);
    }

    // --- Per-user data scope override ---------------------------------------

    @GetMapping("/users/{userId}/scopes")
    public List<ScopeDto> listScopes(@PathVariable Long userId) {
        return accessAdminService.listScopes(userId);
    }

    @PutMapping("/users/{userId}/scopes/{resourceType}")
    public ScopeDto setScope(@PathVariable Long userId,
                             @PathVariable String resourceType,
                             @RequestBody @Valid ScopeUpdateDto dto) {
        // e.g. widen a single user from OWN to ALL for the TICKET resource.
        return accessAdminService.setScope(userId, resourceType, dto.scope());
    }

    @DeleteMapping("/users/{userId}/scopes/{resourceType}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearScope(@PathVariable Long userId,
                           @PathVariable String resourceType) {
        accessAdminService.clearScope(userId, resourceType);
    }

    // --- Role default policy (affects everyone holding the role) -------------

    @GetMapping("/roles/{roleName}/permissions")
    public List<PermissionDto> listRolePermissions(@PathVariable String roleName) {
        return accessAdminService.listRolePermissions(roleName);
    }

    @PutMapping("/roles/{roleName}/permissions")
    public List<PermissionDto> replaceRolePermissions(
            @PathVariable String roleName,
            @RequestBody @Valid RolePermissionsUpdateDto dto) {
        // Bulk replace; bumps the global policy version so shared-role caches
        // are invalidated for every affected user.
        return accessAdminService.replaceRolePermissions(roleName, dto.permissionCodes());
    }
}
