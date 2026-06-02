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

@Component("access")
public class AccessChecker {

    public AccessScope scope(String resourceType) {
        AppUserDetails principal = principal();
        if (principal == null || principal.getScopes() == null) {
            return AccessScope.NONE;
        }
        return principal.getScopes().getOrDefault(normalize(resourceType), AccessScope.NONE);
    }

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

    private String assignedUsername(Object resource) {
        if (resource instanceof Ticket ticket && ticket.getAssignedMember() != null) {
            return ticket.getAssignedMember().getUsername();
        }
        return null;
    }

    private String ownerUsername(Object resource) {
        if (resource instanceof TicketDto ticket) {
            return ticket.getUsername();
        }
        if (resource instanceof Ticket ticket && ticket.getCustomer() != null) {
            return ticket.getCustomer().getUsername();
        }
        return null;
    }

    private Long currentUserId() {
        AppUserDetails principal = principal();
        return principal == null ? null : principal.getId();
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }

    private AppUserDetails principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AppUserDetails principal)) {
            return null;
        }
        return principal;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
