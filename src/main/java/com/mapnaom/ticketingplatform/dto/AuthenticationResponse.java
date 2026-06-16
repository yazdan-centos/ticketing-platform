package com.mapnaom.ticketingplatform.dto;

import com.mapnaom.ticketingplatform.dto.access.PermissionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String currentUser;
    private String accessToken;
    private String role;
}