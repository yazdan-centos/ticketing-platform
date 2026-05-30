package com.mapnaom.ticketingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeamManagerResponseDto extends AppUserResponseDto {
    private String department;
    // List of team member IDs
    private Set<Long> teamMemberIds;
}

