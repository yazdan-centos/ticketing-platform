package com.mapnaom.ticketingplatform.model.enums;

/**
 * Represents the current availability status of agents or support staff in the ticketing platform.
 * <p>
 * This enum is used to track whether an agent is ready to receive new ticket assignments,
 * currently working on existing tickets, or unavailable for work. The status helps with
 * workload distribution and ticket routing decisions.
 * </p>
 */
public enum AvailabilityStatus {
    
    /**
     * Agent is available and ready to receive new ticket assignments.
     * Indicates the agent is logged in, active, and has capacity for additional work.
     */
    AVAILABLE,
    
    /**
     * Agent is currently busy working on assigned tickets.
     * Indicates the agent is actively handling tickets but may still accept urgent assignments.
     */
    BUSY,
    
    /**
     * Agent is off duty and not accepting any ticket assignments.
     * Typically used during breaks, end of shift, or scheduled time off.
     */
    OFF_DUTY,
    
    /**
     * Agent is unavailable for ticket assignments.
     * Used for unexpected absences, system issues, or temporary unavailability.
     */
    UNAVAILABLE
}
