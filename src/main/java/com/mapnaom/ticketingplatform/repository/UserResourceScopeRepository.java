package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.UserResourceScope;
import com.mapnaom.ticketingplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserResourceScopeRepository extends JpaRepository<UserResourceScope, Long> {
    List<UserResourceScope> findByUser(User user);
    Optional<UserResourceScope> findByUserAndResourceType(User user, String resourceType);
    void deleteByUserAndResourceType(User user, String resourceType);
}
