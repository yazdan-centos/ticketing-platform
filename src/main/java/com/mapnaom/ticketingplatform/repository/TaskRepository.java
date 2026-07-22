package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findAllByActiveTrue(Sort sort);

    Optional<Task> findByIdAndActiveTrue(Long id);

    List<Task> findByMeetingIdAndActiveTrueOrderByCreatedAtDesc(Long meetingId);
}
