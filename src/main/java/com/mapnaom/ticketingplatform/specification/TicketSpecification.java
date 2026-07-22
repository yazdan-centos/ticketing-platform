package com.mapnaom.ticketingplatform.specification;

import com.mapnaom.ticketingplatform.dto.TicketSearchRequestDto;
import com.mapnaom.ticketingplatform.model.Ticket;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

            if (request.getCustomerFullName() != null && !request.getCustomerFullName().isBlank()) {
                var customer = root.get("customer");
                var fullName = cb.concat(
                        cb.concat(cb.coalesce(customer.get("firstName"), ""), " "),
                        cb.coalesce(customer.get("lastName"), "")
                );
                predicates.add(cb.like(
                        cb.lower(fullName),
                        "%" + request.getCustomerFullName().trim().toLowerCase(Locale.ROOT) + "%"
                ));
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
