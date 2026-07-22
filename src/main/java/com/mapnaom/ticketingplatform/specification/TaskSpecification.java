package com.mapnaom.ticketingplatform.specification;

import com.mapnaom.ticketingplatform.dto.task.TaskSearchRequestDto;
import com.mapnaom.ticketingplatform.model.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TaskSpecification {
    private TaskSpecification() {}

    public static Specification<Task> bySearchRequest(TaskSearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (request == null) return cb.and(predicates.toArray(new Predicate[0]));
            if (request.getTitle() != null && !request.getTitle().isBlank())
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + request.getTitle().toLowerCase(Locale.ROOT) + "%"));
            if (request.getStatus() != null) predicates.add(cb.equal(root.get("status"), request.getStatus()));
            if (request.getPriority() != null) predicates.add(cb.equal(root.get("priority"), request.getPriority()));
            if (request.getAssignedMemberId() != null) predicates.add(cb.equal(root.get("assignedMember").get("id"), request.getAssignedMemberId()));
            if (request.getDueDateFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("dueDate"), request.getDueDateFrom()));
            if (request.getDueDateTo() != null) predicates.add(cb.lessThanOrEqualTo(root.get("dueDate"), request.getDueDateTo()));
            if (request.getCreatedFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedFrom()));
            if (request.getCreatedTo() != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedTo()));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
