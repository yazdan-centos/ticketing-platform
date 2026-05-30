package com.mapnaom.ticketingplatform.dto.ticket;

import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketSummaryResponse {
    private Long id;
    private String title;
    private TicketStatus status;
    private Long customerId;
    private Long assignedMemberId;
    private LocalDateTime createdAt;
}
