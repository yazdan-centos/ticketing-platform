package com.mapnaom.ticketingplatform.dto.ticket;

import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TicketResponse {

    private Long id;
    private String title;
    private String description;
    private TicketStatus status;

    private Long customerId;
    private Long slaContractId;
    private Long assignedMemberId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<TicketMessageResponse> messages;
    private List<TicketAttachmentResponse> attachments;
    private List<TicketStatusHistoryResponse> statusHistory;
}
