package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.TicketMessage;
import org.springframework.stereotype.Service;

@Service
public interface EmailNotificationService {
    void notifyCustomerNewMessage(Ticket ticket, TicketMessage message);
}
