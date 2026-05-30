package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.ticket.*;
import com.mapnaom.ticketingplatform.model.Ticket;
import com.mapnaom.ticketingplatform.model.TicketAttachment;
import com.mapnaom.ticketingplatform.model.TicketMessage;
import com.mapnaom.ticketingplatform.model.TicketStatusHistory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TicketMapper {

    public TicketSummaryResponse toSummaryResponse(Ticket ticket) {
        TicketSummaryResponse dto = new TicketSummaryResponse();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setStatus(ticket.getStatus());
        dto.setCustomerId(ticket.getCustomer() != null ? ticket.getCustomer().getId() : null);
        dto.setAssignedMemberId(ticket.getAssignedMember() != null ? ticket.getAssignedMember().getId() : null);
        dto.setCreatedAt(ticket.getCreatedAt());
        return dto;
    }

    public TicketResponse toResponse(Ticket ticket) {
        TicketResponse dto = new TicketResponse();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setCustomerId(ticket.getCustomer() != null ? ticket.getCustomer().getId() : null);
        dto.setSlaContractId(ticket.getSlaContract() != null ? ticket.getSlaContract().getId() : null);
        dto.setAssignedMemberId(ticket.getAssignedMember() != null ? ticket.getAssignedMember().getId() : null);
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        dto.setMessages(ticket.getTicketMessages() != null
                ? ticket.getTicketMessages().stream().map(this::toMessageResponse).collect(Collectors.toList())
                : Collections.emptyList());

        dto.setAttachments(ticket.getTicketAttachments() != null
                ? ticket.getTicketAttachments().stream().map(this::toAttachmentResponse).collect(Collectors.toList())
                : Collections.emptyList());

        dto.setStatusHistory(ticket.getTicketStatusHistories() != null
                ? ticket.getTicketStatusHistories().stream().map(this::toStatusHistoryResponse).collect(Collectors.toList())
                : Collections.emptyList());

        return dto;
    }

    public TicketMessageResponse toMessageResponse(TicketMessage message) {
        TicketMessageResponse dto = new TicketMessageResponse();
        dto.setId(message.getId());
        dto.setSenderId(message.getSender() != null ? message.getSender().getId() : null);
        dto.setSenderName(message.getSender() != null ? message.getSender().getUsername() : null);
        dto.setMessage(message.getMessage());
        dto.setSentAt(message.getSentAt());
        return dto;
    }

    public TicketAttachmentResponse toAttachmentResponse(TicketAttachment attachment) {
        TicketAttachmentResponse dto = new TicketAttachmentResponse();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setContentType(attachment.getContentType());
        dto.setSize(attachment.getSize());
        dto.setUploadedById(attachment.getUploadedBy() != null ? attachment.getUploadedBy().getId() : null);
        dto.setUploadedAt(attachment.getUploadedAt());
        return dto;
    }

    public TicketStatusHistoryResponse toStatusHistoryResponse(TicketStatusHistory history) {
        TicketStatusHistoryResponse dto = new TicketStatusHistoryResponse();
        dto.setId(history.getId());
        dto.setOldStatus(history.getOldStatus());
        dto.setNewStatus(history.getNewStatus());
        dto.setChangedById(history.getChangedBy() != null ? history.getChangedBy().getId() : null);
        dto.setChangedByName(history.getChangedBy() != null ? history.getChangedBy().getUsername() : null);
        dto.setNote(history.getNote());
        dto.setChangedAt(history.getChangedAt());
        return dto;
    }
}
