package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.ticket.CustomerTicketDto;
import com.mapnaom.ticketingplatform.dto.ticket.TeamTicketDto;
import com.mapnaom.ticketingplatform.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketCustomerMapper {

    CustomerTicketDto toCustomerDto(Ticket ticket);

    List<CustomerTicketDto> toCustomerDtos(List<Ticket> tickets);

    @Mapping(target = "customerName", expression = "java(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : null)")
    @Mapping(target = "assignedToName", expression = "java(ticket.getAssignedMember() != null ? ticket.getAssignedMember().getFullName() : null)")
    @Mapping(target = "teamName", expression = "java(ticket.getAssignedMember() != null && ticket.getAssignedMember().getManager() != null ? ticket.getAssignedMember().getManager().getDepartment() : null)")
    TeamTicketDto toTeamDto(Ticket ticket);

    List<TeamTicketDto> toTeamDtos(List<Ticket> tickets);
}
