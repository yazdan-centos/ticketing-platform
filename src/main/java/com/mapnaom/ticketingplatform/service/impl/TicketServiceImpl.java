package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.TicketSearchRequestDto;
import com.mapnaom.ticketingplatform.dto.ticket.*;
import com.mapnaom.ticketingplatform.mapper.TicketCustomerMapper;
import com.mapnaom.ticketingplatform.mapper.TicketMapper;
import com.mapnaom.ticketingplatform.model.*;
import com.mapnaom.ticketingplatform.model.enums.AvailabilityStatus;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import com.mapnaom.ticketingplatform.model.enums.UserType;
import com.mapnaom.ticketingplatform.repository.*;
import com.mapnaom.ticketingplatform.service.TicketService;
import com.mapnaom.ticketingplatform.specification.TicketSpecification;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


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
    private final TicketCustomerMapper ticketCustomerMapper;
    // Add these dependencies to TicketServiceImpl
    private final TicketAttachmentRepository ticketAttachmentRepository;
    @Value("${file.upload-dir:/tmp/ticket-uploads}") // Define in application.properties
    private String uploadDir;

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
    public TicketAttachmentResponse attach(Long ticketId, MultipartFile file, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file must not be empty");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        AppUser uploader = appUserRepository.findById(uploaderId)
                .orElseThrow(() -> new EntityNotFoundException("Uploader not found with id: " + uploaderId));

        return storeAttachment(ticket, file, uploader);
    }

    @Override
    public TicketAttachmentResponse attachByCustomer(Long ticketId, MultipartFile file, Long customerId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Attachment file must not be empty");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("Ticket not found with id: " + ticketId));

        AppUser uploader = appUserRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Uploader not found with id: " + customerId));

        if (!(uploader instanceof Customer)) {
            throw new AccessDeniedException("Only customers can upload files using this API");
        }

        if (ticket.getCustomer() == null || !customerId.equals(ticket.getCustomer().getId())) {
            throw new AccessDeniedException("Customers can upload files only to their own tickets");
        }

        return storeAttachment(ticket, file, uploader);
    }

    private TicketAttachmentResponse storeAttachment(Ticket ticket, MultipartFile file, AppUser uploader) {
        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException("Attachment file must have a name");
            }

            String storedFileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(originalFilename);
            Path filePath = dirPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            TicketAttachment attachment = new TicketAttachment();
            attachment.setTicket(ticket);
            attachment.setFileName(originalFilename);
            attachment.setContentType(file.getContentType());
            attachment.setSize(file.getSize());
            attachment.setStorageKey(filePath.toString());
            attachment.setUploadedBy(uploader);

            TicketAttachment savedAttachment = ticketAttachmentRepository.save(attachment);
            log.info("Attachment added: id={}, ticketId={}", savedAttachment.getId(), ticket.getId());

            return ticketMapper.toAttachmentResponse(savedAttachment);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public void detach(Long attachmentId) {
        TicketAttachment attachment = ticketAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + attachmentId));

        try {
            if (attachment.getStorageKey() != null && !attachment.getStorageKey().isBlank()) {
                Path filePath = Paths.get(attachment.getStorageKey());
                Files.deleteIfExists(filePath);
            }

            ticketAttachmentRepository.delete(attachment);
            log.info("Attachment removed: id={}", attachmentId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    @Override
    public List<TicketSummaryResponse> getAll() {
        return ticketRepository.findAll().stream()
                .map(ticketMapper::toSummaryResponse)
                .toList();
    }


    @Override
    public Page<CustomerTicketDto> searchForCustomer(TicketSearchRequestDto request, String sortBy, String order, String pageNumber, String pageSize) {
        Sort sort = order.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(Integer.parseInt(pageNumber), Integer.parseInt(pageSize), sort);

        // Execute the query using the specification + pageable
        Page<Ticket> ticketPage = ticketRepository.findAll(
                TicketSpecification.bySearchRequest(request),
                pageRequest
        );

        // Convert entities -> DTOs
        return ticketPage.map(ticketCustomerMapper::toCustomerDto);
    }

    @Override
    public Page<TeamTicketDto> searchForTeam(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize) {
        Sort sort = order.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        Specification<Ticket> spec = TicketSpecification.bySearchRequest(request);
        Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageRequest);
        return ticketPage.map(ticketCustomerMapper::toTeamDto);
    }

    @Override
    public Page<?> search(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize, Long actorId) {
        if (request == null) {
            throw new IllegalArgumentException("search request must not be null");
        }

        if (actorId == null) {
            throw new IllegalArgumentException("actorId is required for ticket search");
        }

        AppUser actor = appUserRepository.findById(actorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + actorId));
        UserType userType = resolveUserType(actor);

        request.setUserId(actorId);
        request.setUserType(userType);

        return switch (userType) {
            case CUSTOMER -> {
                request.setCustomerId(actorId);
                request.setAssignedToId(null);
                request.setTeamId(null);
                yield searchForCustomer(request, sortBy, order, pageNumber, pageSize);
            }
            case TEAM_MEMBER -> {
                request.setAssignedToId(actorId);
                request.setCustomerId(null);
                yield searchForTeam(request, sortBy, order, pageNumber, pageSize);
            }
            case TEAM_MANAGER -> {
                request.setTeamId(actorId);
                request.setCustomerId(null);
                request.setAssignedToId(null);
                yield searchForTeam(request, sortBy, order, pageNumber, pageSize);
            }
        };
    }

    private UserType resolveUserType(AppUser user) {
        if (user instanceof Customer) {
            return UserType.CUSTOMER;
        }
        if (user instanceof TeamManager) {
            return UserType.TEAM_MANAGER;
        }
        if (user instanceof TeamMember) {
            return UserType.TEAM_MEMBER;
        }
        throw new IllegalArgumentException("Unsupported user type for ticket search: " + user.getClass().getSimpleName());
    }

    @Override
    public Page<CustomerTicketDto> searchForCustomer(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize) {
        Sort sort = order.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        Page<Ticket> ticketPage = ticketRepository.findAll(
                TicketSpecification.bySearchRequest(request),
                pageRequest
        );

        return ticketPage.map(ticketCustomerMapper::toCustomerDto);
    }

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
            case UNALLOCATED -> to == TicketStatus.ASSIGNED || to == TicketStatus.CLOSED; // تخصیص نیافته
            case ASSIGNED ->
                    to == TicketStatus.IN_PROGRESS || to == TicketStatus.UNALLOCATED || to == TicketStatus.CLOSED; // اختصاص یافته
            case IN_PROGRESS ->
                    to == TicketStatus.RESOLVED || to == TicketStatus.CLOSED || to == TicketStatus.ASSIGNED; // در حال انجام
            case RESOLVED -> to == TicketStatus.CLOSED || to == TicketStatus.IN_PROGRESS; // حل شده
            case CLOSED -> false; // terminal state - بسته شده
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
