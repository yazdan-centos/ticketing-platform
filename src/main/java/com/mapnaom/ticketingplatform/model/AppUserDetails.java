package com.mapnaom.ticketingplatform.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AppUserDetails implements UserDetails {

    private final User user;
    private final Set<String> permissionCodes;
    private final Map<String, AccessScope> scopes;

    public AppUserDetails(User user, Set<String> permissionCodes, Map<String, AccessScope> scopes) {
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
        return user.getEmail();
    }

    public Long getId() {
        return user.getId();
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
        return true;
    }
}
