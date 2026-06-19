package com.mapnaom.ticketingplatform.model.enums;

import jakarta.persistence.*;

/**
 * Defines the effect of a permission grant in the access control system.
 * <p>
 * This enum is used in authorization rules to explicitly allow or deny specific actions.
 * It enables fine-grained access control by supporting both positive permissions (ALLOW)
 * and explicit restrictions (DENY). DENY typically takes precedence over ALLOW in
 * permission evaluation logic.
 * </p>
 */
public enum GrantEffect {
    
    /**
     * Grants permission to perform the specified action.
     * When evaluated, this effect permits the user to execute the associated operation.
     */
    ALLOW,
    
    /**
     * Explicitly denies permission to perform the specified action.
     * This effect blocks the user from executing the associated operation,
     * typically overriding any ALLOW grants for the same action.
     */
    DENY
}

