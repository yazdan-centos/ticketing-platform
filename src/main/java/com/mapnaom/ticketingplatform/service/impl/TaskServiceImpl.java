package com.mapnaom.ticketingplatform.service.impl;

import com.mapnaom.ticketingplatform.dto.task.TaskDto;
import com.mapnaom.ticketingplatform.dto.task.TaskSearchRequestDto;
import com.mapnaom.ticketingplatform.mapper.TaskMapper;
import com.mapnaom.ticketingplatform.model.Task;
import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TaskStatus;
import com.mapnaom.ticketingplatform.repository.TaskRepository;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.MeetingRepository;
import com.mapnaom.ticketingplatform.repository.TeamMemberRepository;
import com.mapnaom.ticketingplatform.repository.TeamMembershipRepository;
import com.mapnaom.ticketingplatform.service.TaskService;
import com.mapnaom.ticketingplatform.service.AccessChecker;
import com.mapnaom.ticketingplatform.specification.TaskSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MeetingRepository meetingRepository;
    private final AppUserRepository appUserRepository;
    private final TaskMapper taskMapper;
    private final AccessChecker access;
    private final TeamMembershipRepository membershipRepository;

    @Override
    public TaskDto create(TaskDto dto) {
        Task task = new Task();
        apply(task, dto);
        task.setCreatedBy(appUserRepository.findById(access.currentUserId())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found")));
        access.requireCanSee("TASK", task);
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getAll() {
        return taskRepository.findAll(
                        access.visibleTasks().and((root, query, cb) -> cb.isTrue(root.get("active"))),
                        Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getById(Long id) {
        return taskMapper.toDto(findScoped(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getByMeetingId(Long meetingId) {
        var meeting = meetingRepository.findByIdAndActiveTrue(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("Meeting not found with id: " + meetingId));
        access.requireCanSee("MEETING", meeting);
        return taskRepository.findByMeetingIdAndActiveTrueOrderByCreatedAtDesc(meetingId).stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    public TaskDto update(Long id, TaskDto dto) {
        Task task = findScoped(id);
        apply(task, dto);
        access.requireCanSee("TASK", task);
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    public void delete(Long id) {
        Task task = findScoped(id);
        task.setActive(false);
        taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> search(TaskSearchRequestDto request, String sortBy, String order, int page, int size) {
        String safeSort = List.of("id", "title", "status", "priority", "progress", "dueDate", "createdAt", "updatedAt").contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)), Sort.by(direction, safeSort));
        return taskRepository.findAll(
                TaskSpecification.bySearchRequest(request).and(access.visibleTasks()), pageable).map(taskMapper::toDto);
    }

    private Task find(Long id) {
        return taskRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new EntityNotFoundException("Active task not found with id: " + id));
    }

    private Task findScoped(Long id) {
        Task task = find(id);
        access.requireCanSee("TASK", task);
        return task;
    }

    private void apply(Task task, TaskDto dto) {
        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getPriority() != null) task.setPriority(dto.getPriority());
        if (dto.getProgress() != null) task.setProgress(dto.getProgress());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());
        if (dto.getAssignedMemberId() != null) {
            task.setAssignedMember(teamMemberRepository.findById(dto.getAssignedMemberId())
                    .orElseThrow(() -> new EntityNotFoundException("Team member not found with id: " + dto.getAssignedMemberId())));
        }
        if (dto.getMeetingId() != null) {
            task.setMeeting(meetingRepository.findByIdAndActiveTrue(dto.getMeetingId())
                    .orElseThrow(() -> new EntityNotFoundException("Active meeting not found with id: " + dto.getMeetingId())));
        }
        if (task.getMeeting() != null && task.getAssignedMember() != null
                && !membershipRepository.existsByTeamIdAndUserId(
                task.getMeeting().getTeam().getId(), task.getAssignedMember().getId())) {
            throw new IllegalArgumentException("Assigned member must belong to the meeting team");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            task.setProgress(100);
            if (task.getCompletedAt() == null) task.setCompletedAt(java.time.LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
    }
}
