package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.TicketSearchRequestDto;
import com.mapnaom.ticketingplatform.dto.ticket.*;
import com.mapnaom.ticketingplatform.model.AppUserDetails;
import com.mapnaom.ticketingplatform.service.TicketMessageService;
import com.mapnaom.ticketingplatform.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketMessageService ticketMessageService;

    // --- Create Ticket ---
    // Allowed roles (SecurityConfig): CUSTOMER, TEAM_MANAGER
    @PostMapping
    @PreAuthorize("hasAuthority('TICKET_CREATE')")
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody TicketCreateRequest request) {
        TicketResponse created = ticketService.create(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // --- Get All Tickets (summary view) ---
    // Allowed roles (SecurityConfig): TEAM_MEMBER, TEAM_MANAGER
    @GetMapping
    @PreAuthorize("hasAuthority('TICKET_READ')")
    public ResponseEntity<List<TicketSummaryResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAll());
    }

    // --- Get Ticket By ID (full view with messages, attachments, history) ---
    // Allowed roles (SecurityConfig): CUSTOMER, TEAM_MEMBER, TEAM_MANAGER
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKET_READ')")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id));
    }

    // --- Update Ticket (metadata, SLA, assignment, status transition) ---
    // Allowed roles (SecurityConfig): TEAM_MEMBER, TEAM_MANAGER
    // The acting user is the authenticated principal; it is recorded in the
    // ticket's status history for auditing.
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKET_UPDATE')")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody TicketUpdateRequest request) {
        return ResponseEntity.ok(ticketService.update(id, request, principal.getId()));
    }

    // --- Re-assign Ticket to another team member ---
    // Allowed roles (SecurityConfig): TEAM_MEMBER, TEAM_MANAGER
    // Team members may re-assign tickets assigned to them; team managers may
    // re-assign any ticket within their team. Enforced in the service layer.
    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasAuthority('TICKET_UPDATE')")
    public ResponseEntity<TicketResponse> reassignTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody TicketReassignRequest request) {
        return ResponseEntity.ok(ticketService.reassign(id, request, principal.getId()));
    }

    // --- Delete Ticket ---
    // Allowed roles (SecurityConfig): CUSTOMER, TEAM_MEMBER, TEAM_MANAGER
    // Ownership is enforced in the service layer: customers may delete only
    // tickets they created, team members only tickets assigned to them, and
    // team managers any ticket within their team.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TICKET_DELETE')")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal) {
        ticketService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    // --- Add Message to Ticket ---
    // Allowed roles (SecurityConfig): CUSTOMER, TEAM_MEMBER, TEAM_MANAGER
    // The sender is the authenticated principal.
    @PostMapping("/{id}/messages")
    @PreAuthorize("hasAuthority('TICKET_UPDATE')")
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
    @PreAuthorize("hasAuthority('TICKET_READ')")
    public ResponseEntity<List<TicketMessageResponse>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getById(id).getMessages());
    }


    @PostMapping("/search")
    @PreAuthorize("hasAuthority('TICKET_READ')")
    public Page<?> searchTickets(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestBody TicketSearchRequestDto request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order
    ) {
        return ticketService.search(request, sortBy, order, page, size, principal.getId());
    }


    @PostMapping(value = "/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('TICKET_UPDATE')")
    public ResponseEntity<TicketAttachmentResponse> attachFile(
            @PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestPart("file") MultipartFile file) {
        TicketAttachmentResponse created = ticketService.attach(id, file, principal.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @PreAuthorize("hasAuthority('TICKET_UPDATE')")
    public ResponseEntity<Void> detachFile(@PathVariable Long attachmentId) {
        ticketService.detach(attachmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/attachments/{attachmentId}")
    @PreAuthorize("hasAuthority('TICKET_READ')")
    public ResponseEntity<Resource> getFile(@PathVariable Long attachmentId) {
        return ticketService.getFile(attachmentId);
    }

}
