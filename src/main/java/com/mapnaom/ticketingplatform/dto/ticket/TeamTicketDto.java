package com.mapnaom.ticketingplatform.dto.ticket;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Ticket list projection for the <b>team</b> roles (team member / team manager).
 *
 * <p>Unlike {@link CustomerTicketDto}, this view exposes the internal triage
 * fields {@code priority} and {@code emergency}. Per the access rules the
 * {@code emergency} flag is only meaningful for the assignee team member, so
 * the mapper masks it for non-assignees. The {@code canDelete} /
 * {@code canReassign} flags describe which actions the requesting user may
 * perform on each row.
 */
@Setter
@Getter
public class TeamTicketDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Boolean emergency;
    private String customerName;
    private String assignedToName;
    private String teamName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Role-based action capabilities. Team members may delete / re-assign the
    // tickets currently assigned to them; team managers may act on any ticket
    // within their team.
    private boolean canDelete;
    private boolean canReassign;
}
