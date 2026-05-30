package com.mapnaom.ticketingmanagerserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaContractResponseDto {
    private Long id;
    private String contractName;
    private String serviceScope;
    private Integer responseTimeHours;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested DTO to show customer details
    private CustomerResponseDto customer;
}

