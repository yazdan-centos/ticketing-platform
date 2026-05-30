package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.ticket.*;
import com.mapnaom.ticketingplatform.mapper.TicketMapper;
import com.mapnaom.ticketingplatform.model.*;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.repository.*;
import com.mapnaom.ticketingplatform.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final SlaContractRepository slaContractRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final AppUserRepository appUserRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;
    private final TicketMapper ticketMapper;

    @Override
    public TicketResponse create(TicketCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCustomer(customer);
        ticket.setStatus(TicketStatus.UNALLOCATED);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        if (request.getSlaContractId() != null) {
            SlaContract sla = slaContractRepository.findById(request.getSlaContractId())
                    .orElseThrow(() -> new EntityNotFoundException("SLA contract not found"));
            ticket.setSlaContract(sla);
        }

        if (request.getAssignedMemberId() != null) {
            TeamMember member = teamMemberRepository.findById(request.getAssignedMemberId())
                    .orElseThrow(() -> new EntityNotFoundException("Team member not found"));
            ticket.setAssignedMember(member);
            ticket.setStatus(TicketStatus.ASSIGNED);
        }

        Ticket saved = ticketRepository.save(ticket);
        return ticketMapper.toResponse(saved);
    }

    @Override
    public TicketResponse update(Long ticketId, TicketUpdateRequest request, Long actorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));

        AppUser actor = appUserRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("Actor not found"));

        if (request.getTitle() != null) {
            ticket.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }

        if (request.getSlaContractId() != null) {
            SlaContract sla = slaContractRepository.findById(request.getSlaContractId())
                    .orElseThrow(() -> new EntityNotFoundException("SLA contract not found"));
            ticket.setSlaContract(sla);
        }

        if (request.getAssignedMemberId() != null) {
            TeamMember member = teamMemberRepository.findById(request.getAssignedMemberId())
                    .orElseThrow(() -> new EntityNotFoundException("Team member not found"));
            ticket.setAssignedMember(member);
        }

        if (request.getStatus() != null && request.getStatus() != ticket.getStatus()) {
            TicketStatus oldStatus = ticket.getStatus();
            ticket.setStatus(request.getStatus());

            TicketStatusHistory history = new TicketStatusHistory();
            history.setTicket(ticket);
            history.setOldStatus(oldStatus);
            history.setNewStatus(request.getStatus());
            history.setChangedAt(LocalDateTime.now());
            history.setChangedBy(actor);
            history.setNote("Status updated manually");
            ticketStatusHistoryRepository.save(history);
        }

        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updated = ticketRepository.save(ticket);
        return ticketMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found"));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toSummaryResponse)
                .toList();
    }
}
