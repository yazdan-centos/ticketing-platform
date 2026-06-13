package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.UserResourceScope;
import com.mapnaom.ticketingplatform.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserResourceScopeRepository extends JpaRepository<UserResourceScope, Long> {
    List<UserResourceScope> findByUser(AppUser user);
    Optional<UserResourceScope> findByUserAndResourceType(AppUser user, String resourceType);
    void deleteByUserAndResourceType(AppUser user, String resourceType);
}
