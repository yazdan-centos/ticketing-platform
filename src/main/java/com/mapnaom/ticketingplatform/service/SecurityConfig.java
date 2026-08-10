package com.mapnaom.ticketingplatform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
                        .requestMatchers(HttpMethod.POST, "/api/auth/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/signout").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/uploads/**",
                                "/api/files/download/**",
                                "/api/tickets/attachments/*/download")
                        .permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html").permitAll()

                        // ---- Ticket Messages (more specific than /api/tickets/*) ----
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/messages")
                        .hasAuthority("TICKET_UPDATE")
                        .requestMatchers(HttpMethod.GET, "/api/tickets/*/messages")
                        .hasAuthority("TICKET_READ")
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/attachments")
                        .hasAuthority("TICKET_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/attachments/*")
                        .hasAuthority("TICKET_UPDATE")
                        .requestMatchers(HttpMethod.POST, "/api/team-members/tickets/*/messages")
                        .hasRole(TEAM_MEMBER)
                        .requestMatchers(HttpMethod.POST, "/api/customers/tickets/*/attachments")
                        .hasRole(CUSTOMER)

                        // ---- Tickets ----
                        .requestMatchers(HttpMethod.POST, "/api/tickets/search")
                        .hasAuthority("TICKET_READ")
                        .requestMatchers(HttpMethod.POST, "/api/tickets")
                        .hasAuthority("TICKET_CREATE")
                        .requestMatchers(HttpMethod.GET, "/api/tickets")
                        .hasAuthority("TICKET_READ")
                        .requestMatchers(HttpMethod.GET, "/api/tickets/*")
                        .hasAuthority("TICKET_READ")
                        .requestMatchers(HttpMethod.POST, "/api/tickets/*/reassign")
                        .hasAuthority("TICKET_UPDATE")
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/*")
                        .hasAuthority("TICKET_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/*")
                        .hasAuthority("TICKET_DELETE")

                        // ---- Tasks ----
                        .requestMatchers(HttpMethod.GET, "/api/tasks", "/api/tasks/**")
                        .hasAuthority("TASK_READ")
                        .requestMatchers(HttpMethod.POST, "/api/tasks/search")
                        .hasAuthority("TASK_READ")
                        .requestMatchers(HttpMethod.POST, "/api/tasks")
                        .hasAuthority("TASK_CREATE")
                        .requestMatchers(HttpMethod.PUT, "/api/tasks/*")
                        .hasAuthority("TASK_UPDATE")
                        .requestMatchers(HttpMethod.DELETE, "/api/tasks/*")
                        .hasAuthority("TASK_DELETE")

                        // ---- Customers ----
                        .requestMatchers(HttpMethod.POST, "/api/customers").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/customers").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/customers/options").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/customers/*")
                        .hasAnyRole(CUSTOMER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/customers/*/avatar")
                        .hasAnyRole(CUSTOMER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/customers/*")
                        .hasAnyRole(CUSTOMER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/customers/*").hasRole(TEAM_MANAGER)

                        // ---- Team Members ----
                        .requestMatchers(HttpMethod.POST, "/api/team-members").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/team-members").hasRole(TEAM_MANAGER)
                        .requestMatchers(HttpMethod.GET, "/api/team-members/*")
                        .hasAnyRole(TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/team-members/*/avatar")
                        .hasAnyRole(TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/team-members/*/avatar")
                        .hasAnyRole(TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.PUT, "/api/team-members/*")
                        .hasAnyRole(TEAM_MEMBER, TEAM_MANAGER)
                        .requestMatchers(HttpMethod.DELETE, "/api/team-members/*").hasRole(TEAM_MANAGER)

                        // ---- Team Managers (manager-only across the board) ----
                        .requestMatchers(HttpMethod.POST, "/api/team-managers/*/avatar").hasRole(TEAM_MANAGER)
                        .requestMatchers("/api/team-managers/**").hasRole(TEAM_MANAGER)

                        // ---- SLA Contracts ----
                        .requestMatchers(HttpMethod.GET, "/api/sla-contracts/options")
                        .hasRole(TEAM_MANAGER)
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
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://155.117.13.33:3000",
                "http://155.117.13.33:80",
                "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-ID"));
        config.setExposedHeaders(List.of("X-Total-Count"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }


}
