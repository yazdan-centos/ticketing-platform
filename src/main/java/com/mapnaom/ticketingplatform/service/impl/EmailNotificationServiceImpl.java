package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.TicketMessage;
import com.mapnaom.ticketingplatform.service.EmailNotificationService;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

    @Override
    public void notifyCustomerNewMessage(Ticket ticket, TicketMessage message) {

    }
}
