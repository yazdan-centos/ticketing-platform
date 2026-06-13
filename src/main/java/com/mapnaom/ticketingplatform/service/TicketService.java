package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.ticket.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

public interface TicketService {
    TicketResponse create(TicketCreateRequest request);
    TicketResponse update(Long ticketId, TicketUpdateRequest request, Long actorId);
    TicketResponse getById(Long ticketId);
    TicketAttachmentResponse attach(File file);
    void detach (Long attachmentId);


    @Transactional(readOnly = true)
    List<TicketSummaryResponse> getAll();
}