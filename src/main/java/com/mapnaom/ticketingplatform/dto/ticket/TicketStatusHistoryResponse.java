package com.mapnaom.ticketingplatform.dto.ticket;

import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketStatusHistoryResponse {
    private Long id;
    private TicketStatus oldStatus;
    private TicketStatus newStatus;
    private Long changedById;
    private String changedByName;
    private String note;
    private LocalDateTime changedAt;
}
