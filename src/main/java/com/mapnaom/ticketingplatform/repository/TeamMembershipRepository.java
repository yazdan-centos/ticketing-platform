package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.TeamMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
    List<TeamMembership> findByTeamIdOrderByJoinedAtAsc(Long teamId);

    List<TeamMembership> findByUserId(Long userId);

    Optional<TeamMembership> findByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
}
