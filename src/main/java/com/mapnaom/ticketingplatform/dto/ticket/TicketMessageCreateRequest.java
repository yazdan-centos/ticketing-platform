package com.mapnaom.ticketingplatform.dto.ticket;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TicketMessageCreateRequest {

    // The sender is resolved from the authenticated principal, not the request body.
    @NotBlank
    private String message;
}