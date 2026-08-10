package com.mapnaom.ticketingplatform.controller;

import com.mapnaom.ticketingplatform.dto.task.TaskDto;
import com.mapnaom.ticketingplatform.dto.task.TaskSearchRequestDto;
import com.mapnaom.ticketingplatform.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAuthority('TASK_CREATE')")
    public ResponseEntity<TaskDto> create(@Valid @RequestBody TaskDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(dto));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TASK_READ')")
    public ResponseEntity<List<TaskDto>> getAll() { return ResponseEntity.ok(taskService.getAll()); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_READ')")
    public ResponseEntity<TaskDto> getById(@PathVariable Long id) { return ResponseEntity.ok(taskService.getById(id)); }

    @GetMapping("/meeting/{meetingId}")
    @PreAuthorize("hasAuthority('TASK_READ')")
    public ResponseEntity<List<TaskDto>> getByMeetingId(@PathVariable Long meetingId) {
        return ResponseEntity.ok(taskService.getByMeetingId(meetingId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_UPDATE')")
    public ResponseEntity<TaskDto> update(@PathVariable Long id, @Valid @RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TASK_DELETE')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('TASK_READ')")
    public Page<TaskDto> search(@RequestBody(required = false) TaskSearchRequestDto request,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "createdAt") String sortBy,
                                @RequestParam(defaultValue = "DESC") String order) {
        return taskService.search(request, sortBy, order, page, size);
    }
}
