package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.ticket.TicketListDto;
import com.mapnaom.ticketingplatform.model.Ticket;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TicketListMapper {
    @Mapping(source = "assignedMemberId", target = "assignedMember.id")
    @Mapping(source = "slaContractId", target = "slaContract.id")
    @Mapping(source = "customerId", target = "customer.id")
    Ticket toEntity(TicketListDto ticketListDto);

    @InheritInverseConfiguration(name = "toEntity")
    @Mapping(target = "customerFullName", expression = "java(ticket.getCustomer() != null ? ticket.getCustomer().getFullName() : null)")
    TicketListDto toDto(Ticket ticket);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Ticket partialUpdate(TicketListDto ticketListDto, @MappingTarget Ticket ticket);
}
