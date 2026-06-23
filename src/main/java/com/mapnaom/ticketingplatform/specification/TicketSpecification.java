package com.mapnaom.ticketingplatform.specification;

import com.mapnaom.ticketingplatform.dto.TicketSearchRequestDto;
import com.mapnaom.ticketingplatform.model.Ticket;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TicketSpecification {

    private TicketSpecification() {
    }

    public static Specification<Ticket> bySearchRequest(TicketSearchRequestDto request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getTitle() != null && !request.getTitle().isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("title")),
                                "%" + request.getTitle().toLowerCase() + "%"
                        )
                );
            }

            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getPriority() != null && !request.getPriority().isBlank()) {
                predicates.add(cb.equal(root.get("priority"), request.getPriority()));
            }

            if (request.getCustomerId() != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), request.getCustomerId()));
            }

            if (request.getAssignedToId() != null) {
                predicates.add(cb.equal(root.get("assignedMember").get("id"), request.getAssignedToId()));
            }

            if (request.getTeamId() != null) {
                predicates.add(cb.equal(root.get("assignedMember").get("manager").get("id"), request.getTeamId()));
            }

            if (request.getCreatedFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getCreatedFrom()));
            }

            if (request.getCreatedTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getCreatedTo()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
