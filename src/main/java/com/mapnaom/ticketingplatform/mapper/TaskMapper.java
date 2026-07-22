package com.mapnaom.ticketingplatform.mapper;

import com.mapnaom.ticketingplatform.dto.task.TaskDto;
import com.mapnaom.ticketingplatform.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "assignedMemberId", source = "assignedMember.id")
    @Mapping(target = "meetingId", source = "meeting.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    TaskDto toDto(Task task);
}
