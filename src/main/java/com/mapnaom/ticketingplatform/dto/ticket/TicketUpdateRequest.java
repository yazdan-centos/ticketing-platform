package com.mapnaom.ticketingplatform.dto.ticket;

import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import lombok.Data;

@Data
public class TicketUpdateRequest {

    private String title;
    private String description;
    private Long slaContractId;
    private Long assignedMemberId;
    private TicketStatus status;
}
