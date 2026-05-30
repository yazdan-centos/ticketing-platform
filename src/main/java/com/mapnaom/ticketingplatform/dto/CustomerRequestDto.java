package com.mapnaom.ticketingmanagerserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequestDto {
    private String username;
    private String password; // Only for creation/updates
    private String email;
    private Set<String> roles;
    private String companyName;
    private String phone;
}
