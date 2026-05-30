package com.mapnaom.ticketingplatform.dto.ticket;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TicketAttachmentResponse {
    private Long id;
    private String fileName;
    private String contentType;
    private Long size;
    private String filePath;
    private Long uploadedById;
    private LocalDateTime uploadedAt;
}
