package com.mapnaom.ticketingplatform.specification;

import com.mapnaom.ticketingplatform.model.AppUser;
import org.springframework.data.jpa.domain.Specification;

public final class AppUserSpecification {

    private AppUserSpecification() {
    }

    public static Specification<AppUser> hasUsername(String username) {
        return (root, query, cb) ->
                username == null || username.isBlank()
                        ? cb.conjunction()
                        : cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%");
    }
}