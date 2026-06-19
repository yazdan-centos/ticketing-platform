package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.ticket.TicketDto;
import com.mapnaom.ticketingplatform.dto.ticket.TicketResponse;
import com.mapnaom.ticketingplatform.dto.ticket.TicketSummaryResponse;
import com.mapnaom.ticketingplatform.model.AccessScope;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import com.mapnaom.ticketingplatform.model.Ticket;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

/**
 * Service component for checking user access permissions and authorization across the ticketing platform.
 * <p>
 * This class provides fine-grained access control by evaluating user permissions based on their
 * {@link AccessScope} for specific resource types. It determines whether a user can view or interact
 * with resources (primarily tickets) by comparing the user's scope against the resource's ownership
 * and assignment relationships.
 * </p>
 * <p>
 * The component is registered as a Spring bean with the name "access" for use in SpEL expressions
 * within security annotations (e.g., {@code @PreAuthorize("@access.canSee('ticket', #ticket)")}).
 * </p>
 *
 * @see AccessScope
 * @see AppUserDetails
 */
@Component("access")
public class AccessChecker {

    /**
     * Retrieves the {@link AccessScope} for the current authenticated user on a specific resource type.
     * <p>
     * The scope determines what level of access the user has: NONE, OWN, ASSIGNED, TEAM, or ALL.
     * If no user is authenticated or the user has no defined scopes, returns {@link AccessScope#NONE}.
     * </p>
     *
     * @param resourceType the type of resource to check (e.g., "ticket", "user"), case-insensitive
     * @return the {@link AccessScope} for the specified resource type, or {@link AccessScope#NONE} if not found
     */
    public AccessScope scope(String resourceType) {
        AppUserDetails principal = principal();
        if (principal == null || principal.getScopes() == null) {
            return AccessScope.NONE;
        }
        return principal.getScopes().getOrDefault(normalize(resourceType), AccessScope.NONE);
    }

    /**
     * Checks whether the current authenticated user can see (access) a specific resource.
     * <p>
     * This method evaluates the user's {@link AccessScope} for the resource type and applies
     * the appropriate authorization logic:
     * <ul>
     *   <li>{@link AccessScope#ALL} or {@link AccessScope#TEAM} - grants access to all resources</li>
     *   <li>{@link AccessScope#ASSIGNED} - grants access only if the resource is assigned to the user</li>
     *   <li>{@link AccessScope#OWN} - grants access only if the user owns/created the resource</li>
     *   <li>{@link AccessScope#NONE} - denies all access</li>
     * </ul>
     * </p>
     *
     * @param resourceType the type of resource being checked (e.g., "ticket")
     * @param resource the actual resource object to check access against (must not be null)
     * @return {@code true} if the user can access the resource, {@code false} otherwise
     */
    public boolean canSee(String resourceType, Object resource) {
        if (resource == null) {
            return false;
        }

        AccessScope accessScope = scope(resourceType);
        return switch (accessScope) {
            case ALL, TEAM -> true;
            case ASSIGNED -> matchesAssignedUser(resource);
            case OWN -> matchesOwnerUser(resource);
            case NONE -> false;
        };
    }

    /**
     * Checks if the current user matches the assigned user of the resource.
     * <p>
     * Compares the authenticated user's ID or username with the resource's assigned user.
     * Supports multiple resource types including {@link TicketResponse}, {@link TicketSummaryResponse},
     * and {@link Ticket}.
     * </p>
     *
     * @param resource the resource to check assignment for
     * @return {@code true} if the current user is assigned to the resource, {@code false} otherwise
     */
    private boolean matchesAssignedUser(Object resource) {
        Long currentUserId = currentUserId();
        if (currentUserId != null) {
            Long assignedUserId = assignedUserId(resource);
            if (assignedUserId != null) {
                return Objects.equals(currentUserId, assignedUserId);
            }
        }

        String username = currentUsername();
        return username != null && Objects.equals(username, assignedUsername(resource));
    }

    /**
     * Checks if the current user matches the owner (creator) of the resource.
     * <p>
     * Compares the authenticated user's ID or username with the resource's owner/customer.
     * Supports multiple resource types including {@link TicketResponse}, {@link TicketSummaryResponse},
     * {@link TicketDto}, and {@link Ticket}.
     * </p>
     *
     * @param resource the resource to check ownership for
     * @return {@code true} if the current user owns the resource, {@code false} otherwise
     */
    private boolean matchesOwnerUser(Object resource) {
        Long currentUserId = currentUserId();
        if (currentUserId != null) {
            Long ownerUserId = ownerUserId(resource);
            if (ownerUserId != null) {
                return Objects.equals(currentUserId, ownerUserId);
            }
        }

        String username = currentUsername();
        return username != null && Objects.equals(username, ownerUsername(resource));
    }

    /**
     * Extracts the assigned user ID from the resource.
     * <p>
     * Handles multiple resource types and returns the ID of the user assigned to the resource.
     * </p>
     *
     * @param resource the resource to extract the assigned user ID from
     * @return the assigned user's ID, or {@code null} if not assigned or resource type is unsupported
     */
    private Long assignedUserId(Object resource) {
        if (resource instanceof TicketResponse ticket) {
            return ticket.getAssignedMemberId();
        }
        if (resource instanceof TicketSummaryResponse ticket) {
            return ticket.getAssignedMemberId();
        }
        if (resource instanceof Ticket ticket && ticket.getAssignedMember() != null) {
            return ticket.getAssignedMember().getId();
        }
        return null;
    }

    /**
     * Extracts the owner (customer) user ID from the resource.
     * <p>
     * Handles multiple resource types and returns the ID of the user who owns/created the resource.
     * </p>
     *
     * @param resource the resource to extract the owner user ID from
     * @return the owner's user ID, or {@code null} if not set or resource type is unsupported
     */
    private Long ownerUserId(Object resource) {
        if (resource instanceof TicketResponse ticket) {
            return ticket.getCustomerId();
        }
        if (resource instanceof TicketSummaryResponse ticket) {
            return ticket.getCustomerId();
        }
        if (resource instanceof Ticket ticket && ticket.getCustomer() != null) {
            return ticket.getCustomer().getId();
        }
        return null;
    }

    /**
     * Extracts the assigned user's username from the resource.
     * <p>
     * Currently supports {@link Ticket} entities with assigned members.
     * </p>
     *
     * @param resource the resource to extract the assigned username from
     * @return the assigned user's username, or {@code null} if not assigned or resource type is unsupported
     */
    private String assignedUsername(Object resource) {
        if (resource instanceof Ticket ticket && ticket.getAssignedMember() != null) {
            return ticket.getAssignedMember().getUsername();
        }
        return null;
    }

    /**
     * Extracts the owner's (customer's) username from the resource.
     * <p>
     * Handles multiple resource types including {@link TicketDto} and {@link Ticket}.
     * </p>
     *
     * @param resource the resource to extract the owner username from
     * @return the owner's username, or {@code null} if not set or resource type is unsupported
     */
    private String ownerUsername(Object resource) {
        if (resource instanceof TicketDto ticket) {
            return ticket.getUsername();
        }
        if (resource instanceof Ticket ticket && ticket.getCustomer() != null) {
            return ticket.getCustomer().getUsername();
        }
        return null;
    }

    /**
     * Retrieves the current authenticated user's ID.
     *
     * @return the current user's ID, or {@code null} if no user is authenticated
     */
    private Long currentUserId() {
        AppUserDetails principal = principal();
        return principal == null ? null : principal.getId();
    }

    /**
     * Retrieves the current authenticated user's username.
     *
     * @return the current user's username, or {@code null} if no user is authenticated
     */
    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    /**
     * Retrieves the current authenticated user's principal as {@link AppUserDetails}.
     * <p>
     * Extracts the principal from the Spring Security context and casts it to {@link AppUserDetails}
     * if available.
     * </p>
     *
     * @return the authenticated {@link AppUserDetails}, or {@code null} if no user is authenticated
     *         or the principal is not of type {@link AppUserDetails}
     */
    private AppUserDetails principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
            return null;
        }
        return principal;
    }

    /**
     * Normalizes a string value for consistent comparison.
     * <p>
     * Trims whitespace and converts to uppercase for case-insensitive matching.
     * </p>
     *
     * @param value the string to normalize
     * @return the normalized string (trimmed and uppercase), or empty string if input is {@code null}
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
