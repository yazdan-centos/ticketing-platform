package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.User;
import com.mapnaom.ticketingplatform.model.UserPermissionGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionGrantRepository extends JpaRepository<UserPermissionGrant, Long> {
    List<UserPermissionGrant> findByUser(User user);
    Optional<UserPermissionGrant> findByUserAndPermissionCode(User user, String permissionCode);
    void deleteByUserAndPermissionCode(User user, String permissionCode);
}
