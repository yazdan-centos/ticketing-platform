package com.mapnaom.ticketingplatform.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketMessageCreateRequest {

    @NotNull
    private Long senderId;

    @NotBlank
    private String message;
}