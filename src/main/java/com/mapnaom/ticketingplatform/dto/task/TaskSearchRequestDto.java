package com.mapnaom.ticketingplatform.dto.task;

import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskSearchRequestDto {
    private String title;
    private TaskStatus status;
    private Priority priority;
    private Long assignedMemberId;
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
