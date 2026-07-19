package com.mapnaom.ticketingplatform.dto.ticket;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Ticket list projection for the <b>customer</b> role.
 *
 * <p>Customers own the tickets they create but are not part of the support
 * team, so triage-only fields ({@code priority}, {@code emergency}) are
 * deliberately absent from this view. The {@code canDelete} flag tells the
 * frontend whether the customer may delete a given row – customers can only
 * delete tickets they created.
 */
@Setter
@Getter
public class CustomerTicketDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Role-based action capability. True only for tickets created by the
    // requesting customer.
    private boolean canDelete;
}
