package com.mapnaom.ticketingplatform.dto.task;

import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskDto {
    private Long id;
    @NotBlank
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    @Min(0) @Max(100)
    private Integer progress;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
    private Boolean active;
    private Long assignedMemberId;
    private Long meetingId;
    private Long createdById;
}
