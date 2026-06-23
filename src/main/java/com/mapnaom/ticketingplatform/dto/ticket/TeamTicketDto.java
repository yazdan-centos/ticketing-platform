package com.mapnaom.ticketingplatform.dto.ticket;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
public class TeamTicketDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String customerName;
    private String assignedToName;
    private String teamName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
