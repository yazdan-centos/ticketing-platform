package com.mapnaom.ticketingplatform.dto;

import com.mapnaom.ticketingplatform.model.enums.UserType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TicketSearchRequestDto {
    private String title;
    private String status;
    private String priority;
    private Long customerId;
    private Long assignedToId;
    private Long teamId;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
    private UserType userType;
    private Long userId;
}
