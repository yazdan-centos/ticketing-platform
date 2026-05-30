package com.mapnaom.ticketingplatform.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Long customerId;

    private Long slaContractId;

    private Long assignedMemberId;
}
