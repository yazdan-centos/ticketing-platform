package com.mapnaom.ticketingplatform.specification;

import com.mapnaom.ticketingplatform.dto.TeamMemberSearchCriteriaDto;
import com.mapnaom.ticketingplatform.model.TeamMember;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TeamMemberSpecification {

    public static Specification<TeamMember> filterTeamMembers(TeamMemberSearchCriteriaDto criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getUsername() != null && !criteria.getUsername().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + criteria.getUsername().toLowerCase() + "%"));
            }
            if (criteria.getEmail() != null && !criteria.getEmail().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + criteria.getEmail().toLowerCase() + "%"));
            }
            if (criteria.getFirstName() != null && !criteria.getFirstName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + criteria.getFirstName().toLowerCase() + "%"));
            }
            if (criteria.getLastName() != null && !criteria.getLastName().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + criteria.getLastName().toLowerCase() + "%"));
            }
            // Assuming 'role' is part of the AppUser or a related entity, for now, let's assume it's directly on TeamMember or AppUser
            // If 'role' is a field in AppUser (parent of TeamMember), it can be accessed directly.
            // If it's a separate entity, a join would be needed. For simplicity, assuming it's a direct field or accessible via root.
            if (criteria.getRole() != null && !criteria.getRole().isEmpty()) {
                // This assumes 'role' is a direct string field in TeamMember or its parent AppUser.
                // If 'role' is an enum or a more complex object, this would need adjustment.
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("role")), "%" + criteria.getRole().toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
