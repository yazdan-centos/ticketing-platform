package com.mapnaom.ticketingplatform.dto.ticket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.mapnaom.ticketingplatform.model.Ticket}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TicketListDto implements Serializable {
    private Long id;
    private String title;
    private String description;
    @NotNull
    private TicketStatus status = TicketStatus.UNALLOCATED;
    private Priority priority = Priority.MEDIUM;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long customerId;
    private String customerFullName;
    private Long slaContractId;
    private Long assignedMemberId;
}
