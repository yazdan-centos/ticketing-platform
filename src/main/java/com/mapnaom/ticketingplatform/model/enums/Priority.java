package com.mapnaom.ticketingplatform.model.enums;

/**
 * Defines the priority level of tickets in the ticketing platform.
 * <p>
 * Priority determines the urgency and order in which tickets should be addressed.
 * This enum helps agents and the system prioritize work, allocate resources,
 * and set appropriate service level expectations. Priorities range from LOW
 * (routine requests) to CRITICAL (urgent issues requiring immediate attention).
 * </p>
 */
public enum Priority {
    
    /**
     * Low priority - routine requests with no time pressure.
     * Suitable for general inquiries, feature requests, or minor issues
     * that can be addressed when resources are available.
     */
    LOW,
    
    /**
     * Medium priority - standard requests requiring timely attention.
     * Represents typical support tickets that should be handled within
     * normal service level agreements.
     */
    MEDIUM,
    
    /**
     * High priority - important issues requiring prompt resolution.
     * Used for significant problems affecting user productivity or
     * business operations that need quick attention.
     */
    HIGH,
    
    /**
     * Critical priority - urgent issues requiring immediate action.
     * Reserved for system outages, security incidents, or severe problems
     * that block critical business functions and demand instant response.
     */
    CRITICAL
}
