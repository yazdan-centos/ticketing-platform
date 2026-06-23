package com.mapnaom.ticketingplatform.searchForm;

import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TicketSearchForm {
    private String title;
    private String description;
    private TicketStatus status = TicketStatus.UNALLOCATED;
    private Priority priority = Priority.MEDIUM;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long customerId;
    private Long slaContractId;
    private Long assignedMemberId;
}
