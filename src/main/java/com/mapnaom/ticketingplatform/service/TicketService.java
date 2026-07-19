package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.TicketSearchRequestDto;
import com.mapnaom.ticketingplatform.dto.ticket.*;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface TicketService {
    TicketResponse create(TicketCreateRequest request);
    TicketResponse update(Long ticketId, TicketUpdateRequest request, Long actorId);
    TicketResponse getById(Long ticketId);

    TicketAttachmentResponse attach(Long ticketId, MultipartFile file, Long uploaderId);

    TicketAttachmentResponse attachByCustomer(Long ticketId, MultipartFile file, Long customerId);

    void detach(Long attachmentId);

    @Transactional(readOnly = true)
    List<TicketSummaryResponse> getAll();

    /**
     * Role-aware ticket list search. Resolves the acting user's {@code UserType}
     * and dispatches to the appropriate role-specific projection so that each
     * role only sees the fields and row-level actions it is allowed to.
     *
     * <ul>
     *   <li>Customer  -> {@link CustomerTicketDto} (no priority/emergency; delete only own).</li>
     *   <li>Team member -> {@link TeamTicketDto} (emergency visible only for assignee; delete/reassign own).</li>
     *   <li>Team manager -> {@link TeamTicketDto} (full team visibility; delete/reassign across the team).</li>
     * </ul>
     */
    Page<?> search(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize, Long actorId);

    /**
     * Customer-scoped list search. Each row's {@code canDelete} flag reflects
     * whether the requesting customer created that ticket.
     */
    Page<CustomerTicketDto> searchForCustomer(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize, Long customerId);

    /**
     * Team-scoped list search. Row-level {@code emergency} visibility and the
     * {@code canDelete}/{@code canReassign} flags are resolved for the acting
     * user; managers may act across their team, members only on tickets
     * assigned to them.
     */
    Page<TeamTicketDto> searchForTeam(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize, Long actorId, boolean isManager);

    /**
     * Deletes a ticket on behalf of the acting user, enforcing role-based
     * ownership: a customer may delete only tickets they created, a team member
     * only tickets assigned to them, and a team manager any ticket within their
     * team.
     */
    void delete(Long ticketId, Long actorId);

    /**
     * Re-assigns a ticket to another team member on behalf of the acting user.
     * Only the current assignee team member or a team manager overseeing the
     * assignee may re-assign; customers may never re-assign.
     */
    TicketResponse reassign(Long ticketId, TicketReassignRequest request, Long actorId);
}
