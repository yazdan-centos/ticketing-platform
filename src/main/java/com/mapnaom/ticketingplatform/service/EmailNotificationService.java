package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.TicketMessage;

public interface EmailNotificationService {
    void notifyCustomerNewMessage(Ticket ticket, TicketMessage message);
}
