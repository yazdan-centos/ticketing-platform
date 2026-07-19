package com.mapnaom.ticketingplatform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDto {
    private Long id;
    private String username;
    private String email;
    private String avatarUrl;
    private Set<String> roles;
    private Set<String> permissions;
}
