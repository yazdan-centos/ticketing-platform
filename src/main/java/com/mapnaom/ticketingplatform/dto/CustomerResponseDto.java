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
public class CustomerResponseDto extends AppUserResponseDto {
    private String companyName;
    private String phone;
    // Including IDs of contracts to avoid infinite recursion
    private Set<Long> slaContractIds;
}

