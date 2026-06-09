package com.mapnaom.ticketingplatform.dto;

import lombok.Data;

@Data
public class TeamMemberSearchCriteriaDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role; // Assuming role is a string for simplicity
}