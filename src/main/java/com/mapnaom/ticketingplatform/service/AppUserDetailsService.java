package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.model.*;
import com.mapnaom.ticketingplatform.model.enums.GrantEffect;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.UserPermissionGrantRepository;
import com.mapnaom.ticketingplatform.repository.UserResourceScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads the authenticated {@link AppUser} and resolves its effective access:
 * role-default permissions adjusted by per-user ALLOW/DENY grants, plus the
 * data-scope map (role default for each resource type, overridden by any
 * {@link UserResourceScope} rows).
 */
@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepo;
    private final UserPermissionGrantRepository grantRepo;
    private final UserResourceScopeRepository scopeRepo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        AppUser user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        Set<String> codes = new HashSet<>();
        user.getRoles().forEach(role ->
                role.getPermissions().forEach(permission -> codes.add(permission.getCode())));

        for (UserPermissionGrant g : grantRepo.findByUser(user)) {
            if (g.getEffect() == GrantEffect.ALLOW) codes.add(g.getPermission().getCode());
            else codes.remove(g.getPermission().getCode());
        }

        Map<String, AccessScope> scopes = resolveScopes(user); // role default + override

        return new AppUserDetails(user, codes, scopes);
    }

    private Map<String, AccessScope> resolveScopes(AppUser user) {
        Map<String, AccessScope> scopes = new LinkedHashMap<>();
        scopes.put("TICKET", defaultTicketScope(user));
        scopeRepo.findByUser(user).forEach(scope ->
                scopes.put(scope.getResourceType(), scope.getScope()));
        return scopes;
    }

    private AccessScope defaultTicketScope(AppUser user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        if (roleNames.contains("TEAM_MANAGER") || roleNames.contains("ADMIN")) {
            return AccessScope.ALL;
        }
        if (roleNames.contains("TEAM_MEMBER")) {
            return AccessScope.ASSIGNED;
        }
        if (roleNames.contains("CUSTOMER")) {
            return AccessScope.OWN;
        }
        return AccessScope.NONE;
    }
}
