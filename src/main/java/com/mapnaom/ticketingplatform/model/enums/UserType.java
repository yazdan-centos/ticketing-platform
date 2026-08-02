package com.mapnaom.ticketingplatform.model.enums;

/**
 * Represents the different types of users within the ticketing platform.
 * This enum is used to enforce role-based access control and determine user capabilities.
 */
public enum UserType {
    /**
     * Represents a standard end-user who purchases tickets and submits support requests.
     * Application: Used to restrict access to customer-facing portals and ticket submission forms.
     */
    CUSTOMER,

    /**
     * Represents a staff member who handles and resolves support tickets.
|      * Application: Used to grant access to the ticket management dashboard and assign tickets for resolution.
     */
    TEAM_MEMBER,

    /**
     * Represents a managerial user who oversees team members and monitors ticketing metrics.
     * Application: Used to grant access to administrative features, reporting dashboards, and team management tools.
     */
    TEAM_MANAGER
}
