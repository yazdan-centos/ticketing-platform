package com.mapnaom.ticketingplatform.model.enums;


/**
 * Represents the lifecycle status of a ticket in the ticketing platform.
 * <p>
 * This enum tracks tickets through their complete workflow from initial creation
 * to final resolution. The status transitions typically follow a sequential flow:
 * UNALLOCATED → ASSIGNED → IN_PROGRESS → RESOLVED/CLOSED. Understanding these
 * states is crucial for workflow automation, reporting, and SLA tracking.
 * </p>
 */
public enum TicketStatus {
    
    /**
     * Ticket has been created but not yet assigned to any agent.
     * This is the initial state for new tickets awaiting assignment
     * through manual or automatic routing.
     */
    UNALLOCATED,
    
    /**
     * Ticket has been assigned to an agent but work has not started.
     * The ticket is in the agent's queue awaiting their attention.
     */
    ASSIGNED,
    
    /**
     * Agent is actively working on resolving the ticket.
     * Indicates ongoing investigation, troubleshooting, or implementation
     * of a solution.
     */
    IN_PROGRESS,
    
    /**
     * Ticket has been closed without resolution.
     * Used for duplicates, spam, invalid requests, or tickets closed
     * by the requester before resolution.
     */
    CLOSED,
    
    /**
     * Ticket has been successfully resolved and completed.
     * The issue has been addressed, solution has been provided,
     * and the ticket is ready for closure after confirmation.
     */
    RESOLVED
}
