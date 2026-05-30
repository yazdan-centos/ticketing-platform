package com.mapnaom.ticketingplatform.dto.ticket;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketMessageResponse {
    private Long id;
    private Long senderId;
    private String senderName;
    private String message;
    private LocalDateTime sentAt;
}
