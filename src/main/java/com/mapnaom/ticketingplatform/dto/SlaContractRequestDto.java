package com.mapnaom.ticketingplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlaContractRequestDto {
    private String contractName;
    private String serviceScope;
    private Integer responseTimeHours;
    private Boolean isActive;
    private Long customerId; // Foreign key to Customer
}
