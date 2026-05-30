package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.ticket.*;
import com.mapnaom.ticketingplatform.service.TicketMessageService;
import com.mapnaom.ticketingplatform.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POST /api/tickets → create ticket
 * PUT /api/tickets/{id} → update metadata/status/assignment
 * POST /api/tickets/{id}/messages → add message
 * POST /api/tickets/{id}/attachments → upload file
 * GET /api/tickets/{id} → full detail
 * GET /api/tickets → summary list
 * **/

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketMessageService ticketMessageService;

    @PostMapping
    public TicketResponse create(@Valid @RequestBody TicketCreateRequest request) {
        return ticketService.create(request);
    }

    @PutMapping("/{ticketId}")
    public TicketResponse update(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketUpdateRequest request,
            @RequestParam Long actorId
    ) {
        return ticketService.update(ticketId, request, actorId);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse getById(@PathVariable Long ticketId) {
        return ticketService.getById(ticketId);
    }

    @GetMapping
    public List<TicketSummaryResponse> getAll() {
        return ticketService.getAll();
    }

    @PostMapping("/{ticketId}/messages")
    public TicketMessageResponse addMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketMessageCreateRequest request
    ) {
        return ticketMessageService.addMessage(ticketId, request);
    }
}
