package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.UserPermissionGrant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionGrantRepository extends JpaRepository<UserPermissionGrant, Long> {
    List<UserPermissionGrant> findByUser(AppUser user);
    Optional<UserPermissionGrant> findByUserAndPermissionCode(AppUser user, String permissionCode);
    void deleteByUserAndPermissionCode(AppUser user, String permissionCode);
}
