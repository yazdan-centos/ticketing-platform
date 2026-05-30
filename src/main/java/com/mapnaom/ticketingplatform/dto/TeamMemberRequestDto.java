package com.mapnaom.ticketingmanagerserver.dto;

import com.mapnaom.ticketingmanagerserver.model.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberRequestDto {
    private String username;
    private String password;
    private String email;
    private Set<String> roles;
    private AvailabilityStatus availabilityStatus;
    private String jobTitle;
    private Long managerId; // ID of the TeamManager
}
