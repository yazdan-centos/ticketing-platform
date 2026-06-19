package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.access.EffectiveAccessDto;
import com.mapnaom.ticketingplatform.dto.access.GrantDto;
import com.mapnaom.ticketingplatform.dto.access.PermissionDto;
import com.mapnaom.ticketingplatform.dto.access.ScopeDto;
import com.mapnaom.ticketingplatform.model.AccessScope;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.Permission;
import com.mapnaom.ticketingplatform.model.Role;
import com.mapnaom.ticketingplatform.model.UserPermissionGrant;
import com.mapnaom.ticketingplatform.model.UserResourceScope;
import com.mapnaom.ticketingplatform.model.enums.GrantEffect;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.PermissionRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import com.mapnaom.ticketingplatform.repository.UserPermissionGrantRepository;
import com.mapnaom.ticketingplatform.repository.UserResourceScopeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Administrative service for managing user access control, permissions, roles, and resource scopes.
 * <p>
 * This service provides comprehensive access management functionality including:
 * <ul>
 *   <li>User permission grants with ALLOW/DENY effects</li>
 *   <li>Resource scope assignments (e.g., ticket access levels)</li>
 *   <li>Role-to-permission mappings</li>
 *   <li>User-to-role assignments</li>
 *   <li>Effective access calculation combining roles, grants, and scopes</li>
 * </ul>
 * </p>
 * <p>
 * The service follows a hierarchical access model where:
 * <ul>
 *   <li>Users inherit permissions from their assigned roles</li>
 *   <li>Individual permission grants (ALLOW/DENY) can override role permissions</li>
 *   <li>Resource scopes define visibility boundaries (NONE, OWN, ASSIGNED, TEAM, ALL)</li>
 *   <li>Explicit user scopes override default role-based scopes</li>
 * </ul>
 * </p>
 *
 * @see GrantEffect
 * @see AccessScope
 * @see UserPermissionGrant
 * @see UserResourceScope
 */
@Service
@RequiredArgsConstructor
public class AccessAdminService {

    private static final String TICKET_RESOURCE = "TICKET";

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionGrantRepository grantRepository;
    private final UserResourceScopeRepository scopeRepository;

    /**
     * Retrieves all available permissions in the system.
     * <p>
     * Returns a complete list of permission definitions sorted alphabetically by code.
     * </p>
     *
     * @return list of all permissions as {@link PermissionDto}, sorted by permission code
     */
    @Transactional(readOnly = true)
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(this::toPermissionDto)
                .toList();
    }

    /**
     * Calculates and returns the effective access rights for a specific user.
     * <p>
     * Computes the combined access by merging:
     * <ul>
     *   <li>Role names assigned to the user</li>
     *   <li>Effective permissions (role-based + individual grants)</li>
     *   <li>Effective resource scopes (explicit scopes + role-based defaults)</li>
     * </ul>
     * </p>
     *
     * @param userId the ID of the user to calculate effective access for
     * @return {@link EffectiveAccessDto} containing roles, permissions, and scopes
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public EffectiveAccessDto getEffectiveAccess(Long userId) {
        AppUser user = getUser(userId);
        return new EffectiveAccessDto(
                user.getId(),
                roleNames(user),
                effectivePermissionCodes(user),
                effectiveScopes(user));
    }

    /**
     * Retrieves all individual permission grants for a specific user.
     * <p>
     * Returns explicit ALLOW or DENY grants that override role-based permissions.
     * Does not include permissions inherited from roles.
     * </p>
     *
     * @param userId the ID of the user
     * @return list of {@link GrantDto} representing individual permission grants, sorted by permission code
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<GrantDto> listGrants(Long userId) {
        AppUser user = getUser(userId);
        return grantRepository.findByUser(user).stream()
                .sorted(Comparator.comparing(grant -> grant.getPermission().getCode()))
                .map(this::toGrantDto)
                .toList();
    }

    /**
     * Creates or updates an individual permission grant for a user.
     * <p>
     * If a grant already exists for the specified permission, it updates the effect.
     * Otherwise, creates a new grant. These grants override permissions inherited from roles.
     * </p>
     *
     * @param userId the ID of the user to grant permission to
     * @param permissionCode the code of the permission to grant (case-insensitive)
     * @param effect the effect of the grant ({@link GrantEffect#ALLOW} or {@link GrantEffect#DENY})
     * @return {@link GrantDto} representing the created or updated grant
     * @throws EntityNotFoundException if the user or permission does not exist
     */
    @Transactional
    public GrantDto upsertGrant(Long userId, String permissionCode, GrantEffect effect) {
        AppUser user = getUser(userId);
        Permission permission = getPermission(permissionCode);
        UserPermissionGrant grant = grantRepository
                .findByUserAndPermissionCode(user, permission.getCode())
                .orElseGet(UserPermissionGrant::new);

        grant.setUser(user);
        grant.setPermission(permission);
        grant.setEffect(effect);

        return toGrantDto(grantRepository.save(grant));
    }

    /**
     * Removes an individual permission grant for a user.
     * <p>
     * After removal, the user's access to the permission reverts to what is defined by their roles.
     * </p>
     *
     * @param userId the ID of the user
     * @param permissionCode the code of the permission grant to remove (case-insensitive)
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional
    public void removeGrant(Long userId, String permissionCode) {
        AppUser user = getUser(userId);
        grantRepository.deleteByUserAndPermissionCode(user, normalize(permissionCode));
    }

    /**
     * Retrieves all resource scope assignments for a specific user.
     *
     * @param userId the ID of the user
     * @return list of {@link ScopeDto} representing resource scopes, sorted by resource type
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<ScopeDto> listScopes(Long userId) {
        AppUser user = getUser(userId);
        return scopeRepository.findByUser(user).stream()
                .sorted(Comparator.comparing(UserResourceScope::getResourceType))
                .map(this::toScopeDto)
                .toList();
    }

    /**
     * Sets or updates a resource scope for a user.
     * <p>
     * Defines the level of access (NONE, OWN, ASSIGNED, TEAM, ALL) the user has for a specific
     * resource type. This overrides the default scope derived from the user's roles.
     * </p>
     *
     * @param userId the ID of the user
     * @param resourceType the type of resource (e.g., "ticket"), case-insensitive
     * @param scope the {@link AccessScope} to assign
     * @return {@link ScopeDto} representing the created or updated scope
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional
    public ScopeDto setScope(Long userId, String resourceType, AccessScope scope) {
        AppUser user = getUser(userId);
        String normalizedResourceType = normalize(resourceType);
        UserResourceScope userScope = scopeRepository
                .findByUserAndResourceType(user, normalizedResourceType)
                .orElseGet(UserResourceScope::new);

        userScope.setUser(user);
        userScope.setResourceType(normalizedResourceType);
        userScope.setScope(scope);

        return toScopeDto(scopeRepository.save(userScope));
    }

    /**
     * Removes a resource scope assignment for a user.
     * <p>
     * After removal, the user's scope for the resource type reverts to the default
     * determined by their roles.
     * </p>
     *
     * @param userId the ID of the user
     * @param resourceType the type of resource (case-insensitive)
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional
    public void clearScope(Long userId, String resourceType) {
        AppUser user = getUser(userId);
        scopeRepository.deleteByUserAndResourceType(user, normalize(resourceType));
    }

    /**
     * Retrieves all permissions assigned to a specific role.
     *
     * @param roleName the name of the role (case-insensitive)
     * @return list of {@link PermissionDto} for the role, sorted by permission code
     * @throws EntityNotFoundException if the role does not exist
     */
    @Transactional(readOnly = true)
    public List<PermissionDto> listRolePermissions(String roleName) {
        return getRole(roleName).getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(this::toPermissionDto)
                .toList();
    }

    /**
     * Replaces all permissions for a role with a new set of permissions.
     * <p>
     * This operation completely overwrites the role's existing permissions.
     * Any permissions not included in the new list will be removed from the role.
     * </p>
     *
     * @param roleName the name of the role to update (case-insensitive)
     * @param permissionCodes list of permission codes to assign to the role
     * @return list of {@link PermissionDto} representing the updated role permissions, sorted by code
     * @throws EntityNotFoundException if the role or any permission does not exist
     */
    @Transactional
    public List<PermissionDto> replaceRolePermissions(String roleName, List<String> permissionCodes) {
        Role role = getRole(roleName);
        Set<Permission> permissions = permissionCodes.stream()
                .map(this::getPermission)
                .collect(Collectors.toSet());

        role.setPermissions(permissions);

        return roleRepository.save(role).getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(this::toPermissionDto)
                .toList();
    }

    /**
     * Grants a role to a user, adding it to their existing roles.
     * <p>
     * The user will inherit all permissions and default scopes associated with the role.
     * </p>
     *
     * @param userId the ID of the user to grant the role to
     * @param roleName the name of the role to grant (case-insensitive)
     * @throws EntityNotFoundException if the user or role does not exist
     */
    @Transactional
    public void grantAccess(Long userId, String roleName) {
        AppUser user = getUser(userId);
        user.getRoles().add(getRole(roleName));
        userRepository.save(user);
    }

    /**
     * Revokes a role from a user, removing it from their assigned roles.
     * <p>
     * The user will lose permissions and default scopes associated with the role,
     * unless granted through other roles or individual grants/scopes.
     * </p>
     *
     * @param userId the ID of the user to revoke the role from
     * @param roleName the name of the role to revoke (case-insensitive)
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional
    public void revokeAccess(Long userId, String roleName) {
        AppUser user = getUser(userId);
        String normalizedRoleName = normalize(roleName);
        user.getRoles().removeIf(role -> normalizedRoleName.equals(role.getName()));
        userRepository.save(user);
    }

    /**
     * Retrieves the list of role names assigned to a user.
     *
     * @param userId the ID of the user
     * @return list of role names, sorted alphabetically
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public List<String> getUserAccessRoles(Long userId) {
        return getUser(userId).getRoles().stream()
                .map(Role::getName)
                .sorted()
                .toList();
    }

    /**
     * Checks whether a user has a specific permission.
     * <p>
     * Evaluates the user's effective permissions (considering roles and grants) to determine
     * if they have the specified permission.
     * </p>
     *
     * @param userId the ID of the user to check
     * @param permission the permission code to check (case-insensitive)
     * @return {@code true} if the user has the permission, {@code false} otherwise
     * @throws EntityNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public boolean checkAccess(Long userId, String permission) {
        return effectivePermissionCodes(getUser(userId)).contains(normalize(permission));
    }

    /**
     * Calculates the effective set of permission codes for a user.
     * <p>
     * Combines permissions from all assigned roles and applies individual grants:
     * <ul>
     *   <li>Starts with all permissions from user's roles</li>
     *   <li>Applies ALLOW grants (adds permissions)</li>
     *   <li>Applies DENY grants (removes permissions)</li>
     * </ul>
     * DENY grants take precedence over ALLOW grants and role permissions.
     * </p>
     *
     * @param user the user to calculate effective permissions for
     * @return set of permission codes the user effectively has
     */
    private Set<String> effectivePermissionCodes(AppUser user) {
        Set<String> codes = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toSet());

        grantRepository.findByUser(user).forEach(grant -> {
            String code = grant.getPermission().getCode();
            if (grant.getEffect() == GrantEffect.ALLOW) {
                codes.add(code);
            } else {
                codes.remove(code);
            }
        });

        return codes.stream().sorted().collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    /**
     * Calculates the effective resource scopes for a user.
     * <p>
     * Returns a map of resource types to their effective {@link AccessScope}:
     * <ul>
     *   <li>Explicit user scopes take highest precedence</li>
     *   <li>For TICKET resource without explicit scope, uses role-based default</li>
     * </ul>
     * </p>
     *
     * @param user the user to calculate effective scopes for
     * @return map of resource type to {@link AccessScope}
     */
    private Map<String, AccessScope> effectiveScopes(AppUser user) {
        Map<String, AccessScope> scopes = new LinkedHashMap<>();
        scopes.put(TICKET_RESOURCE, defaultTicketScope(user));
        scopeRepository.findByUser(user).forEach(scope ->
                scopes.put(scope.getResourceType(), scope.getScope()));
        return scopes;
    }

    /**
     * Determines the default ticket access scope based on user's roles.
     * <p>
     * Role-based defaults:
     * <ul>
     *   <li>ADMIN or TEAM_MANAGER → ALL (full access)</li>
     *   <li>TEAM_MEMBER → ASSIGNED (assigned tickets only)</li>
     *   <li>CUSTOMER → OWN (own tickets only)</li>
     *   <li>No recognized role → NONE (no access)</li>
     * </ul>
     * </p>
     *
     * @param user the user to determine default scope for
     * @return the default {@link AccessScope} for tickets based on roles
     */
    private AccessScope defaultTicketScope(AppUser user) {
        Set<String> names = roleNames(user);
        if (names.contains("TEAM_MANAGER") || names.contains("ADMIN")) {
            return AccessScope.ALL;
        }
        if (names.contains("TEAM_MEMBER")) {
            return AccessScope.ASSIGNED;
        }
        if (names.contains("CUSTOMER")) {
            return AccessScope.OWN;
        }
        return AccessScope.NONE;
    }

    /**
     * Extracts role names from a user's assigned roles.
     *
     * @param user the user to extract role names from
     * @return set of role names, preserving insertion order
     */
    private Set<String> roleNames(AppUser user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    /**
     * Retrieves a user by ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the {@link AppUser} entity
     * @throws EntityNotFoundException if the user does not exist
     */
    private AppUser getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    /**
     * Retrieves a role by name (case-insensitive).
     *
     * @param roleName the name of the role to retrieve
     * @return the {@link Role} entity
     * @throws EntityNotFoundException if the role does not exist
     */
    private Role getRole(String roleName) {
        return roleRepository.findByName(normalize(roleName))
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
    }

    /**
     * Retrieves a permission by code (case-insensitive).
     *
     * @param permissionCode the code of the permission to retrieve
     * @return the {@link Permission} entity
     * @throws EntityNotFoundException if the permission does not exist
     */
    private Permission getPermission(String permissionCode) {
        return permissionRepository.findByCode(normalize(permissionCode))
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + permissionCode));
    }

    /**
     * Normalizes a string value for consistent comparison.
     * <p>
     * Trims whitespace and converts to uppercase for case-insensitive matching.
     * </p>
     *
     * @param value the string to normalize
     * @return the normalized string (trimmed and uppercase), or {@code null} if input is {@code null}
     */
    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    /**
     * Converts a {@link Permission} entity to a DTO.
     *
     * @param permission the permission entity
     * @return {@link PermissionDto} representation
     */
    private PermissionDto toPermissionDto(Permission permission) {
        return new PermissionDto(permission.getCode(), permission.getDescription());
    }

    /**
     * Converts a {@link UserPermissionGrant} entity to a DTO.
     *
     * @param grant the grant entity
     * @return {@link GrantDto} representation
     */
    private GrantDto toGrantDto(UserPermissionGrant grant) {
        return new GrantDto(grant.getPermission().getCode(), grant.getEffect());
    }

    /**
     * Converts a {@link UserResourceScope} entity to a DTO.
     *
     * @param scope the scope entity
     * @return {@link ScopeDto} representation
     */
    private ScopeDto toScopeDto(UserResourceScope scope) {
        return new ScopeDto(scope.getResourceType(), scope.getScope());
    }
}
