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

@Service
@RequiredArgsConstructor
public class AccessAdminService {

    private static final String TICKET_RESOURCE = "TICKET";

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionGrantRepository grantRepository;
    private final UserResourceScopeRepository scopeRepository;

    @Transactional(readOnly = true)
    public List<PermissionDto> listPermissions() {
        return permissionRepository.findAll().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(this::toPermissionDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EffectiveAccessDto getEffectiveAccess(Long userId) {
        AppUser user = getUser(userId);
        return new EffectiveAccessDto(
                user.getId(),
                roleNames(user),
                effectivePermissionCodes(user),
                effectiveScopes(user));
    }

    @Transactional(readOnly = true)
    public List<GrantDto> listGrants(Long userId) {
        AppUser user = getUser(userId);
        return grantRepository.findByUser(user).stream()
                .sorted(Comparator.comparing(grant -> grant.getPermission().getCode()))
                .map(this::toGrantDto)
                .toList();
    }

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

    @Transactional
    public void removeGrant(Long userId, String permissionCode) {
        AppUser user = getUser(userId);
        grantRepository.deleteByUserAndPermissionCode(user, normalize(permissionCode));
    }

    @Transactional(readOnly = true)
    public List<ScopeDto> listScopes(Long userId) {
        AppUser user = getUser(userId);
        return scopeRepository.findByUser(user).stream()
                .sorted(Comparator.comparing(UserResourceScope::getResourceType))
                .map(this::toScopeDto)
                .toList();
    }

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

    @Transactional
    public void clearScope(Long userId, String resourceType) {
        AppUser user = getUser(userId);
        scopeRepository.deleteByUserAndResourceType(user, normalize(resourceType));
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> listRolePermissions(String roleName) {
        return getRole(roleName).getPermissions().stream()
                .sorted(Comparator.comparing(Permission::getCode))
                .map(this::toPermissionDto)
                .toList();
    }

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

    @Transactional
    public void grantAccess(Long userId, String roleName) {
        AppUser user = getUser(userId);
        user.getRoles().add(getRole(roleName));
        userRepository.save(user);
    }

    @Transactional
    public void revokeAccess(Long userId, String roleName) {
        AppUser user = getUser(userId);
        String normalizedRoleName = normalize(roleName);
        user.getRoles().removeIf(role -> normalizedRoleName.equals(role.getName()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<String> getUserAccessRoles(Long userId) {
        return getUser(userId).getRoles().stream()
                .map(Role::getName)
                .sorted()
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean checkAccess(Long userId, String permission) {
        return effectivePermissionCodes(getUser(userId)).contains(normalize(permission));
    }

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

    private Map<String, AccessScope> effectiveScopes(AppUser user) {
        Map<String, AccessScope> scopes = new LinkedHashMap<>();
        scopes.put(TICKET_RESOURCE, defaultTicketScope(user));
        scopeRepository.findByUser(user).forEach(scope ->
                scopes.put(scope.getResourceType(), scope.getScope()));
        return scopes;
    }

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

    private Set<String> roleNames(AppUser user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private AppUser getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private Role getRole(String roleName) {
        return roleRepository.findByName(normalize(roleName))
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
    }

    private Permission getPermission(String permissionCode) {
        return permissionRepository.findByCode(normalize(permissionCode))
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + permissionCode));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private PermissionDto toPermissionDto(Permission permission) {
        return new PermissionDto(permission.getCode(), permission.getDescription());
    }

    private GrantDto toGrantDto(UserPermissionGrant grant) {
        return new GrantDto(grant.getPermission().getCode(), grant.getEffect());
    }

    private ScopeDto toScopeDto(UserResourceScope scope) {
        return new ScopeDto(scope.getResourceType(), scope.getScope());
    }
}
