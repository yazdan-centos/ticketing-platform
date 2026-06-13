package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageCreateRequest;
import com.mapnaom.ticketingplatform.dto.ticket.TicketMessageResponse;
import com.mapnaom.ticketingplatform.mapper.TicketMapper;
import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.TeamMember;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.TicketMessage;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.TicketMessageRepository;
import com.mapnaom.ticketingplatform.repository.TicketRepository;
import com.mapnaom.ticketingplatform.service.EmailNotificationService;
import com.mapnaom.ticketingplatform.service.TicketMessageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketMessageServiceImpl implements TicketMessageService {

    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final TicketMapper ticketMapper;
    private final EmailNotificationService emailNotificationService;

    @Override
    public TicketMessageResponse addMessage(Long ticketId, TicketMessageCreateRequest request, Long senderId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        AppUser sender = appUserRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));

        TicketMessage message = new TicketMessage();
        message.setTicket(ticket);
        message.setSender(sender);
        message.setMessage(request.getMessage());
        message.setSentAt(LocalDateTime.now());

        TicketMessage saved = ticketMessageRepository.save(message);

        if (sender instanceof TeamMember) {
            emailNotificationService.notifyCustomerNewMessage(ticket, saved);
        }

        return ticketMapper.toMessageResponse(saved);
    }
}
