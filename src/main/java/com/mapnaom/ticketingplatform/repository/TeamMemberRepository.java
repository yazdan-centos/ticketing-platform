package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}