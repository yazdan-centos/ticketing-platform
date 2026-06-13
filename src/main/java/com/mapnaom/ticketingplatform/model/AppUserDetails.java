package com.mapnaom.ticketingplatform.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring Security principal backed by the domain {@link AppUser}.
 *
 * <p>The authenticated principal <em>is</em> the application user, so business
 * code can resolve the acting user directly from the security context (no
 * separate auth/domain user bridge required). Effective permission codes and
 * resource scopes are pre-computed at load time (role defaults + per-user
 * grants/scope overrides) and exposed here.
 */
public class AppUserDetails implements UserDetails {

    private final AppUser user;
    private final Set<String> permissionCodes;
    private final Map<String, AccessScope> scopes;

    public AppUserDetails(AppUser user, Set<String> permissionCodes, Map<String, AccessScope> scopes) {
        this.user = user;
        this.permissionCodes = permissionCodes;
        this.scopes = scopes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(
                        user.getRoles().stream().map(role -> "ROLE_" + role.getName()),
                        permissionCodes.stream())
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /** Domain user id (an {@code app_users} row). */
    public Long getId() {
        return user.getId();
    }

    /** The underlying domain user, for callers that need the entity. */
    public AppUser getAppUser() {
        return user;
    }

    public Set<String> getPermissionCodes() {
        return permissionCodes;
    }

    public Map<String, AccessScope> getScopes() {
        return scopes;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Soft-deleted users cannot authenticate.
        return !Boolean.TRUE.equals(user.getDeleted());
    }
}
