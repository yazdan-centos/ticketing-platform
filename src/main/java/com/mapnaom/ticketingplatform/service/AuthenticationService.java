package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.AuthenticationRequest;
import com.mapnaom.ticketingplatform.dto.AuthenticationResponse;
import com.mapnaom.ticketingplatform.dto.auth.CurrentUserDto;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Throws AuthenticationException on bad credentials -> handled as 401
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(request.getUsername());

        String accessToken = jwtService.generateToken(userDetails);
        // Authorities mix role names (ROLE_*) and bare permission codes; surface
        // the primary role rather than whichever authority happens to be first.
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .findFirst()
                .orElse(null);

        return AuthenticationResponse.builder()
                .currentUser(userDetails.getUsername())
                .accessToken(accessToken)
                .role(role)
                .avatarUrl(userDetails instanceof AppUserDetails details
                        ? details.getAppUser().getAvatarUrl()
                        : null)
                .build();
    }

    public CurrentUserDto currentUser(AppUserDetails principal) {
        AppUser user = principal.getAppUser();
        return new CurrentUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()),
                principal.getPermissionCodes());
    }
}
