package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.ticket.TicketCreateRequest;
import com.mapnaom.ticketingplatform.dto.ticket.TicketResponse;
import com.mapnaom.ticketingplatform.dto.ticket.TicketSummaryResponse;
import com.mapnaom.ticketingplatform.dto.ticket.TicketUpdateRequest;

import java.util.List;

public interface TicketService {
    TicketResponse create(TicketCreateRequest request);
    TicketResponse update(Long ticketId, TicketUpdateRequest request, Long actorId);
    TicketResponse getById(Long ticketId);
    List<TicketSummaryResponse> getAll();
}