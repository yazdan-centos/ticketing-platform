package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.model.*;
import com.mapnaom.ticketingplatform.model.enums.GrantEffect;
import com.mapnaom.ticketingplatform.repository.UserPermissionGrantRepository;
import com.mapnaom.ticketingplatform.repository.UserRepository;
import com.mapnaom.ticketingplatform.repository.UserResourceScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;
    private final UserPermissionGrantRepository grantRepo;
    private final UserResourceScopeRepository scopeRepo;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        Set<String> codes = new HashSet<>();
        user.getRoles().forEach(r -> {
            Optional.ofNullable(r).ifPresent(role ->
                    role.getPermissions().forEach(permission -> codes.add(permission.getCode())));
        });

        for (UserPermissionGrant g : grantRepo.findByUser(user)) {
            if (g.getEffect() == GrantEffect.ALLOW) codes.add(g.getPermission().getCode());
            else codes.remove(g.getPermission().getCode());
        }

        Map<String, AccessScope> scopes = resolveScopes(user); // role default + override

        return new AppUserDetails(user, codes, scopes);
    }

    private Map<String, AccessScope> resolveScopes(User user) {
        Map<String, AccessScope> scopes = new LinkedHashMap<>();
        scopes.put("TICKET", defaultTicketScope(user));
        scopeRepo.findByUser(user).forEach(scope ->
                scopes.put(scope.getResourceType(), scope.getScope()));
        return scopes;
    }

    private AccessScope defaultTicketScope(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());

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
