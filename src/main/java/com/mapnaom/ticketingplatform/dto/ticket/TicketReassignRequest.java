package com.mapnaom.ticketingplatform.dto.ticket;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for re-assigning a ticket to another team member.
 *
 * <p>Used by the ticket-list "re-assign" action available to team members
 * (for tickets assigned to them) and team managers (for tickets within their
 * team).
 */
@Data
public class TicketReassignRequest {

    @NotNull
    private Long assignedMemberId;

    /** Optional note recorded in the ticket status history. */
    private String note;
}
