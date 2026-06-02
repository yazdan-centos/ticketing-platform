package com.mapnaom.ticketingplatform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    private static final String CUSTOMER = "CUSTOMER";
    private static final String TEAM_MEMBER = "TEAM_MEMBER";
    private static final String TEAM_MANAGER = "TEAM_MANAGER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ---- Public ----
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()

                        // ---- Ticket Messages (more specific than /api/tickets/*) ----
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/messages")
                        .hasAnyRole(CUSTOMER, TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/tickets/*/messages")
                        .hasAnyRole(CUSTOMER, TEAM_MEMBER, TEAM_MANAGER)

                        // ---- Tickets ----
                        .requestMatchers(HttpMethod.POST, "/api/tickets")
                        .hasAnyRole(CUSTOMER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/tickets")
                        .hasAnyRole(TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/tickets/*")
                        .hasAnyRole(CUSTOMER, TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/*")
                        .hasAnyRole(TEAM_MEMBER, TEAM_MANAGER)

                        // ---- Customers ----
                        .requestMatchers(HttpMethod.POST, "/api/customers").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/customers").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/customers/*")
                        .hasAnyRole(CUSTOMER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/customers/*")
                        .hasAnyRole(CUSTOMER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/*").hasRole(TEAM_MANAGER)

                        // ---- Team Members ----
                        .requestMatchers(HttpMethod.POST, "/api/team-members").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/team-members").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/team-members/*")
                        .hasAnyRole(TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/team-members/*")
                        .hasAnyRole(TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/team-members/*").hasRole(TEAM_MANAGER)

                        // ---- Team Managers (manager-only across the board) ----
                        .requestMatchers("/api/team-managers/**").hasRole(TEAM_MANAGER)

                        // ---- SLA Contracts ----
                        .requestMatchers(HttpMethod.GET, "/api/sla-contracts/*")
                        .hasAnyRole(CUSTOMER, TEAM_MANAGER)
                        .requestMatchers("/api/sla-contracts/**").hasRole(TEAM_MANAGER)

                        // ---- Everything else ----
                        .anyRequest().authenticated())

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}