package com.mapnaom.ticketingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamManagerRequestDto {
    private String username;
    private String password;
    private String email;
    private Set<String> roles;
    private String department;
}
