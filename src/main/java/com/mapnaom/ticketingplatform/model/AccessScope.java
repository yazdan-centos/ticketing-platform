package com.mapnaom.ticketingplatform.model;

/**
 * Defines the scope of access control for tickets and related entities in the ticketing platform.
 * <p>
 * This enum is used to determine which tickets a user can view, modify, or interact with
 * based on their relationship to the ticket (creator, assignee, team member, etc.).
 * The scopes are ordered from most restrictive (NONE) to least restrictive (ALL).
 * </p>
 *
 * @see com.mapnaom.ticketingplatform.model.Ticket
 */
public enum AccessScope {
    
    /**
     * No access - user cannot view or interact with any tickets.
     * Typically used for disabled or restricted accounts.
     */
    NONE,
    
    /**
     * Own tickets only - user can only access tickets they created.
     * Suitable for basic users who can only see their own submitted tickets.
     */
    OWN,
    
    /**
     * Assigned tickets - user can access tickets assigned to them.
     * Typically used for agents or support staff who work on assigned tickets.
     */
    ASSIGNED,
    
    /**
     * Team tickets - user can access all tickets within their team(s).
     * Suitable for team leads or members who need visibility across team workload.
     */
    TEAM,
    
    /**
     * All tickets - user can access all tickets in the system.
     * Reserved for administrators and supervisors with full platform visibility.
     */
    ALL
}

