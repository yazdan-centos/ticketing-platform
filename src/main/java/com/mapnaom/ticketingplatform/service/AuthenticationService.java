package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.AuthenticationRequest;
import com.mapnaom.ticketingplatform.dto.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
        String role = userDetails.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        return AuthenticationResponse.builder()
                .currentUser(userDetails.getUsername())
                .accessToken(accessToken)
                .role(role)
                .build();
    }
}