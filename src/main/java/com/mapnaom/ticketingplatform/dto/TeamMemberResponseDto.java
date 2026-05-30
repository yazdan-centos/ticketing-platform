package com.mapnaom.ticketingmanagerserver.dto;

import com.mapnaom.ticketingmanagerserver.model.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeamMemberResponseDto extends AppUserResponseDto {
    private AvailabilityStatus availabilityStatus;
    private String jobTitle;
    // Reference to manager by ID to avoid recursion
    private Long managerId;
}

