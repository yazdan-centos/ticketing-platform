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

    void detach(Long attachmentId);

    @Transactional(readOnly = true)
    List<TicketSummaryResponse> getAll();

    Page<?> search(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize, Long actorId);

    Page<CustomerTicketDto> searchForCustomer(TicketSearchRequestDto request, String sortBy, String order, String pageNumber, String pageSize);

    Page<CustomerTicketDto> searchForCustomer(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize);

    Page<TeamTicketDto> searchForTeam(TicketSearchRequestDto request, String sortBy, String order, int pageNumber, int pageSize);
}
