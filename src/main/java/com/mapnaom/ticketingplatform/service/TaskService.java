package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.dto.task.TaskDto;
import com.mapnaom.ticketingplatform.dto.task.TaskSearchRequestDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {
    TaskDto create(TaskDto dto);
    List<TaskDto> getAll();
    TaskDto getById(Long id);
    List<TaskDto> getByMeetingId(Long meetingId);
    TaskDto update(Long id, TaskDto dto);
    void delete(Long id);
    Page<TaskDto> search(TaskSearchRequestDto request, String sortBy, String order, int page, int size);
}
