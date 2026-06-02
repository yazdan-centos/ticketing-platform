package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.ticket.*;
import com.mapnaom.ticketingplatform.mapper.TicketMapper;
import com.mapnaom.ticketingplatform.model.*;
import com.mapnaom.ticketingplatform.model.enums.AvailabilityStatus;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.repository.*;
import com.mapnaom.ticketingplatform.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
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

    /**
     * Creates a new ticket.
     *
     * <p>The initial status is {@link TicketStatus#UNALLOCATED}. If an
     * {@code assignedMemberId} is provided the status is promoted to
     * {@link TicketStatus#ASSIGNED} automatically, provided the target member
     * is not off-duty or unavailable.
     *
     * <p>When an SLA contract is supplied the ticket's due-date is computed
     * from the contract's response-time so that SLA tracking can begin
     * immediately.
     */
    @Override
    public TicketResponse create(TicketCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Customer not found with id: " + request.getCustomerId()));

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCustomer(customer);
        ticket.setStatus(TicketStatus.UNALLOCATED);

        if (request.getSlaContractId() != null) {
            SlaContract sla = findActiveSlaContract(request.getSlaContractId());
            ticket.setSlaContract(sla);
            // Compute due-date from SLA response-time so the ticket is SLA-tracked from creation
            if (sla.getResponseTimeHours() != null) {
                ticket.setDueDate(ticket.getCreatedAt() != null
                        ? ticket.getCreatedAt().plusHours(sla.getResponseTimeHours())
                        : java.time.LocalDateTime.now().plusHours(sla.getResponseTimeHours()));
            }
        }

        if (request.getAssignedMemberId() != null) {
            TeamMember member = findAvailableTeamMember(request.getAssignedMemberId());
            ticket.setAssignedMember(member);
            ticket.setStatus(TicketStatus.ASSIGNED);
        }

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket created: id={}, customerId={}, status={}", saved.getId(), saved.getCustomer().getId(), saved.getStatus());
        return ticketMapper.toResponse(saved);
    }

    /**
     * Updates an existing ticket's metadata, SLA contract, assignment, or status.
     *
     * <p>Status transitions are validated against the allowed transition matrix.
     * Every status change is recorded in {@link TicketStatusHistory} with the
     * acting user and an optional note supplied via the request.
     *
     * <p>Reassigning a ticket to a new member while it is already
     * {@link TicketStatus#IN_PROGRESS} keeps the status as-is; the status
     * only moves to {@link TicketStatus#ASSIGNED} when the previous status
     * was {@link TicketStatus#UNALLOCATED}.
     */
    @Override
    public TicketResponse update(Long ticketId, TicketUpdateRequest request, Long actorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ticket not found with id: " + ticketId));

        AppUser actor = appUserRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Actor not found with id: " + actorId));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            ticket.setTitle(request.getTitle());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            ticket.setDescription(request.getDescription());
        }

        if (request.getSlaContractId() != null) {
            SlaContract sla = findActiveSlaContract(request.getSlaContractId());
            ticket.setSlaContract(sla);
        }

        if (request.getAssignedMemberId() != null) {
            TeamMember member = findAvailableTeamMember(request.getAssignedMemberId());
            ticket.setAssignedMember(member);
            // Only auto-promote to ASSIGNED when the ticket is still UNALLOCATED
            if (ticket.getStatus() == TicketStatus.UNALLOCATED) {
                recordStatusChange(ticket, TicketStatus.ASSIGNED, actor, "Ticket assigned to team member");
                ticket.setStatus(TicketStatus.ASSIGNED);
            }
        }

        if (request.getStatus() != null) {
            applyStatusTransition(ticket, request.getStatus(), actor, request.getStatusNote());
        }

        Ticket updated = ticketRepository.save(ticket);
        log.info("Ticket updated: id={}, status={}, actorId={}", updated.getId(), updated.getStatus(), actorId);
        return ticketMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Ticket not found with id: " + ticketId));
        return ticketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toSummaryResponse)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Validates that a status transition is permitted and records it in history.
     * Throws {@link IllegalArgumentException} for invalid transitions.
     */
    private void applyStatusTransition(Ticket ticket, TicketStatus newStatus, AppUser actor, String note) {
        TicketStatus currentStatus = ticket.getStatus();
        if (currentStatus == newStatus) {
            return; // no-op – idempotent
        }
        validateStatusTransition(currentStatus, newStatus);
        String resolvedNote = (note != null && !note.isBlank()) ? note : "Status updated to " + newStatus;
        recordStatusChange(ticket, newStatus, actor, resolvedNote);
        ticket.setStatus(newStatus);
    }

    /**
     * Enforces the allowed status-transition matrix.
     *
     * <pre>
     * UNALLOCATED  → ASSIGNED | CLOSED
     * ASSIGNED     → IN_PROGRESS | UNALLOCATED | CLOSED
     * IN_PROGRESS  → RESOLVED | CLOSED | ASSIGNED
     * RESOLVED     → CLOSED | IN_PROGRESS   (re-open if issue recurs)
     * CLOSED       → (terminal – no transitions allowed)
     * </pre>
     */
    private void validateStatusTransition(TicketStatus from, TicketStatus to) {
        boolean allowed = switch (from) {
            case UNALLOCATED  -> to == TicketStatus.ASSIGNED   || to == TicketStatus.CLOSED;
            case ASSIGNED     -> to == TicketStatus.IN_PROGRESS || to == TicketStatus.UNALLOCATED || to == TicketStatus.CLOSED;
            case IN_PROGRESS  -> to == TicketStatus.RESOLVED   || to == TicketStatus.CLOSED       || to == TicketStatus.ASSIGNED;
            case RESOLVED     -> to == TicketStatus.CLOSED     || to == TicketStatus.IN_PROGRESS;
            case CLOSED       -> false; // terminal state
        };
        if (!allowed) {
            throw new IllegalArgumentException(
                    String.format("Invalid status transition: %s → %s", from, to));
        }
    }

    /**
     * Persists a new {@link TicketStatusHistory} entry for an in-progress transition.
     * Called before {@code ticket.setStatus()} so {@code oldStatus} is still current.
     */
    private void recordStatusChange(Ticket ticket, TicketStatus newStatus, AppUser actor, String note) {
        TicketStatusHistory history = new TicketStatusHistory();
        history.setTicket(ticket);
        history.setOldStatus(ticket.getStatus());
        history.setNewStatus(newStatus);
        history.setChangedBy(actor);
        history.setNote(note);
        ticketStatusHistoryRepository.save(history);
    }

    /**
     * Looks up a team member and guards against assigning tickets to members
     * who are off-duty or otherwise unavailable.
     */
    private TeamMember findAvailableTeamMember(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Team member not found with id: " + memberId));
        AvailabilityStatus availability = member.getAvailabilityStatus();
        if (availability == AvailabilityStatus.OFF_DUTY || availability == AvailabilityStatus.UNAVAILABLE) {
            throw new IllegalArgumentException(
                    String.format("Team member %d is not available for assignment (status: %s)",
                            memberId, availability));
        }
        return member;
    }

    /**
     * Looks up an SLA contract and ensures it is still active before
     * attaching it to a ticket.
     */
    private SlaContract findActiveSlaContract(Long contractId) {
        SlaContract sla = slaContractRepository.findById(contractId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "SLA contract not found with id: " + contractId));
        if (Boolean.FALSE.equals(sla.getIsActive())) {
            throw new IllegalArgumentException(
                    "SLA contract " + contractId + " is no longer active");
        }
        return sla;
    }
}
