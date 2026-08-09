package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.ticket.TicketDto;
import com.mapnaom.ticketingplatform.dto.ticket.TicketResponse;
import com.mapnaom.ticketingplatform.dto.ticket.TicketSummaryResponse;
import com.mapnaom.ticketingplatform.model.enums.AccessScope;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import com.mapnaom.ticketingplatform.model.Meeting;
import com.mapnaom.ticketingplatform.model.Task;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.repository.MeetingParticipantRepository;
import com.mapnaom.ticketingplatform.repository.TeamMembershipRepository;
import com.mapnaom.ticketingplatform.specification.ResourceScopeSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
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
@RequiredArgsConstructor
public class AccessChecker {

    private final TeamMembershipRepository membershipRepository;
    private final MeetingParticipantRepository participantRepository;

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
            case ALL -> true;
            case TEAM -> matchesTeam(resource);
            case ASSIGNED -> matchesAssignedUser(resource);
            case OWN -> matchesOwnerUser(resource);
            case NONE -> false;
        };
    }

    public void requireCanSee(String resourceType, Object resource) {
        if (!canSee(resourceType, resource)) {
            throw new EntityNotFoundException(resourceType + " not found");
        }
    }

    public void requireTeamAccess(String resourceType, Long teamId) {
        AccessScope accessScope = scope(resourceType);
        if (accessScope == AccessScope.ALL) {
            return;
        }
        Long userId = currentUserId();
        if (accessScope != AccessScope.TEAM || userId == null
                || !membershipRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new EntityNotFoundException(resourceType + " scope not found");
        }
    }

    public Specification<Ticket> visibleTickets() {
        return ResourceScopeSpecification.tickets(scope("TICKET"), currentUserId(), currentManagerId());
    }

    public Specification<Meeting> visibleMeetings() {
        return ResourceScopeSpecification.meetings(scope("MEETING"), currentUserId());
    }

    public Specification<Task> visibleTasks() {
        return ResourceScopeSpecification.tasks(scope("TASK"), currentUserId(), currentManagerId());
    }

    public Long currentUserId() {
        AppUserDetails principal = principal();
        if (principal == null) {
            throw new EntityNotFoundException("Authenticated user not found");
        }
        return principal.getId();
    }

    public boolean hasAllScope(String resourceType) {
        return scope(resourceType) == AccessScope.ALL;
    }

    private boolean matchesTeam(Object resource) {
        Long userId = currentUserId();
        if (resource instanceof Meeting meeting) {
            return meeting.getTeam() != null
                    && membershipRepository.existsByTeamIdAndUserId(meeting.getTeam().getId(), userId);
        }
        if (resource instanceof Task task) {
            if (task.getMeeting() != null) {
                return task.getMeeting().getTeam() != null
                        && membershipRepository.existsByTeamIdAndUserId(task.getMeeting().getTeam().getId(), userId);
            }
            return task.getAssignedMember() != null && usersShareTeam(userId, task.getAssignedMember());
        }
        if (resource instanceof Ticket ticket) {
            return ticket.getAssignedMember() != null && usersShareTeam(userId, ticket.getAssignedMember());
        }
        return false;
    }

    private boolean usersShareTeam(Long userId, TeamMember otherMember) {
        if (Objects.equals(userId, otherMember.getId())) {
            return true;
        }
        if (membershipRepository.usersShareTeam(userId, otherMember.getId())) {
            return true;
        }
        Long managerId = currentManagerId();
        return managerId != null && otherMember.getManager() != null
                && Objects.equals(managerId, otherMember.getManager().getId());
    }

    private Long currentManagerId() {
        AppUserDetails principal = principal();
        if (principal != null && principal.getAppUser() instanceof TeamMember member
                && member.getManager() != null) {
            return member.getManager().getId();
        }
        return null;
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
        if (resource instanceof Task task && task.getAssignedMember() != null) {
            return task.getAssignedMember().getId();
        }
        if (resource instanceof Meeting meeting) {
            return participantRepository.existsByMeetingIdAndUserId(meeting.getId(), currentUserId())
                    ? currentUserId() : null;
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
        if (resource instanceof Task task && task.getCreatedBy() != null) {
            return task.getCreatedBy().getId();
        }
        if (resource instanceof Meeting meeting && meeting.getOrganizer() != null) {
            return meeting.getOrganizer().getId();
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
