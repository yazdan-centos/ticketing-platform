package com.mapnaom.ticketingplatform.specification;

import com.mapnaom.ticketingplatform.dto.ticket.TicketSearchCriteriaDto;
import com.mapnaom.ticketingplatform.model.Ticket;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> filterTickets(TicketSearchCriteriaDto criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getUsername() != null && !criteria.getUsername().isEmpty()) {
                // Using like for partial matches or equal for exact matches
                predicates.add(cb.like(root.get("username"), "%" + criteria.getUsername() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}