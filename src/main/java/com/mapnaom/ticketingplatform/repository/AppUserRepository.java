package com.mapnaom.ticketingmanagerserver.repository;

import com.mapnaom.ticketingmanagerserver.model.AppUser;
import com.mapnaom.ticketingmanagerserver.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    // Custom query to find only TeamMembers, ignoring Customers/Managers
    // This leverages the @DiscriminatorValue("MEMBER")
    @Query("SELECT m FROM TeamMember m")
    List<TeamMember> findAllTeamMembers();
}