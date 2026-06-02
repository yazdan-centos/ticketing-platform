package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.ticket.TicketDto;
import com.mapnaom.ticketingplatform.model.AccessScope;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("access")
public class AccessChecker {

    public AccessScope scope(String resourceType) {
        return principal().getScopes().getOrDefault(resourceType, AccessScope.NONE);
    }

    public boolean canSee(String resourceType, Object resource) {
        AccessScope s = scope(resourceType);
        String me = principal().getUsername();
        return switch (s) {
            case ALL, TEAM -> true;
            case ASSIGNED  -> me.equals(((TicketDto) resource).getAssigneeUsername());
            case OWN       -> me.equals(((TicketDto) resource).getOwnerUsername());
            case NONE      -> false;
        };
    }

    private AppUserDetails principal() {
        return (AppUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

}
