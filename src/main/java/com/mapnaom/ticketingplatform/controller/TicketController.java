package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.ticket.TicketDto;
import com.mapnaom.ticketingplatform.dto.ticket.TicketSearchCriteriaDto;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import com.mapnaom.ticketingplatform.specification.TicketSpecification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/search")
    public List<TicketDto> searchTickets(TicketSearchCriteriaDto criteria) {
        List<Ticket> tickets = ticketRepository.findAll(TicketSpecification.filterTickets(criteria));
        
        return tickets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TicketDto convertToDto(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setUsername(ticket.getAssignedMember().getUsername());
        return dto;
    }
}