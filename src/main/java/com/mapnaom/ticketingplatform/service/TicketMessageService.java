package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageCreateRequest;
import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageResponse;

public interface TicketMessageService {
    TicketMessageResponse addMessage(Long ticketId, TicketMessageCreateRequest request);
}
