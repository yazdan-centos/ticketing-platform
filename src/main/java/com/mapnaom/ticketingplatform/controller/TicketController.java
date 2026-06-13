package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.ticket.TicketCreateRequest;
import com.mapnaom.ticketingplatform.dto.ticket.TicketDto;
import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageCreateRequest;
import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageResponse;
import com.mapnaom.ticketingplatform.dto.ticket.TicketResponse;
import com.mapnaom.ticketingplatform.dto.ticket.TicketSearchCriteriaDto;
import com.mapnaom.ticketingplatform.dto.ticket.TicketSummaryResponse;
import com.mapnaom.ticketingplatform.dto.ticket.TicketUpdateRequest;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import com.mapnaom.ticketingplatform.service.TicketMessageService;
import com.mapnaom.ticketingplatform.service.TicketService;
import com.mapnaom.ticketingplatform.specification.TicketSpecification;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;
    private final TicketService ticketService;
    private final TicketMessageService ticketMessageService;

    // --- Create Ticket ---
    // Allowed roles (SecurityConfig): CUSTOMER, TEAM_MANAGER
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse created = ticketService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // --- Get All Tickets (summary view) ---
    // Allowed roles (SecurityConfig): TEAM_MEMBER, TEAM_MANAGER
    @GetMapping
    public ResponseEntity<List<TicketSummaryResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAll());
    }

    // --- Get Ticket By ID (full view with messages, attachments, history) ---
    // Allowed roles (SecurityConfig): CUSTOMER, TEAM_MEMBER, TEAM_MANAGER
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    // --- Update Ticket (metadata, SLA, assignment, status transition) ---
    // Allowed roles (SecurityConfig): TEAM_MEMBER, TEAM_MANAGER
    // The acting user is the authenticated principal; it is recorded in the
    // ticket's status history for auditing.
    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody TicketUpdateRequest request) {
        return ResponseEntity.ok(ticketService.update(id, request, principal.getId()));
    }

    // --- Add Message to Ticket ---
    // Allowed roles (SecurityConfig): CUSTOMER, TEAM_MEMBER, TEAM_MANAGER
    // The sender is the authenticated principal.
    @PostMapping("/{id}/messages")
    public ResponseEntity<TicketMessageResponse> addMessage(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody TicketMessageCreateRequest request) {
        TicketMessageResponse created = ticketMessageService.addMessage(id, request, principal.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // --- List Messages for a Ticket ---
    // Allowed roles (SecurityConfig): CUSTOMER, TEAM_MEMBER, TEAM_MANAGER
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<TicketMessageResponse>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id).getMessages());
    }

    // --- Search Tickets (existing endpoint, repository-backed) ---
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
        dto.setUsername(ticket.getAssignedMember() != null ? ticket.getAssignedMember().getUsername() : null);
        return dto;
    }
}