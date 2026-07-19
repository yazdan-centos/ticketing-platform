package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.ticket.CustomerTicketDto;
import com.mapnaom.ticketingplatform.dto.ticket.TeamTicketDto;
import com.mapnaom.ticketingplatform.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketCustomerMapper {

    // -------------------------------------------------------------------------
    // Customer view – priority & emergency are intentionally never mapped here.
    // -------------------------------------------------------------------------
    @Mapping(target = "canDelete", ignore = true)
    CustomerTicketDto toCustomerDto(Ticket ticket);

    List<CustomerTicketDto> toCustomerDtos(List<Ticket> tickets);

    /**
     * Customer-scoped projection that also resolves the row-level delete
     * capability. Customers may delete only tickets they created.
     */
    default CustomerTicketDto toCustomerDto(Ticket ticket, Long customerId) {
        CustomerTicketDto dto = toCustomerDto(ticket);
        if (dto != null) {
            boolean owner = customerId != null
                    && ticket.getCustomer() != null
                    && customerId.equals(ticket.getCustomer().getId());
            dto.setCanDelete(owner);
        }
        return dto;
    }

    // -------------------------------------------------------------------------
    // Team view – exposes priority; emergency is masked for non-assignees.
    // -------------------------------------------------------------------------
    @Mapping(target = "customerName", expression = "java(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : null)")
    @Mapping(target = "assignedToName", expression = "java(ticket.getAssignedMember() != null ? ticket.getAssignedMember().getFullName() : null)")
    @Mapping(target = "teamName", expression = "java(ticket.getAssignedMember() != null && ticket.getAssignedMember().getManager() != null ? ticket.getAssignedMember().getManager().getDepartment() : null)")
    @Mapping(target = "emergency", ignore = true)
    @Mapping(target = "canDelete", ignore = true)
    @Mapping(target = "canReassign", ignore = true)
    TeamTicketDto toTeamDto(Ticket ticket);

    List<TeamTicketDto> toTeamDtos(List<Ticket> tickets);

    /**
     * Team-scoped projection resolving row-level capabilities and emergency
     * visibility for the acting user.
     *
     * @param ticket    the ticket entity
     * @param actorId   the authenticated user id
     * @param isManager {@code true} when the actor is a team manager
     */
    default TeamTicketDto toTeamDto(Ticket ticket, Long actorId, boolean isManager) {
        TeamTicketDto dto = toTeamDto(ticket);
        if (dto == null) {
            return null;
        }
        boolean assignee = actorId != null
                && ticket.getAssignedMember() != null
                && actorId.equals(ticket.getAssignedMember().getId());

        // Emergency is an assignee-only signal; managers oversee the team so
        // they can see it too. It is masked (null) for everyone else.
        dto.setEmergency(assignee || isManager ? ticket.isEmergency() : null);

        // Team members may delete / re-assign tickets assigned to them.
        // Managers may act on any ticket in their team scope.
        dto.setCanDelete(assignee || isManager);
        dto.setCanReassign(assignee || isManager);
        return dto;
    }
}
