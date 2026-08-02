package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.TeamMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, Long> {
    List<TeamMembership> findByTeamIdOrderByJoinedAtAsc(Long teamId);

    List<TeamMembership> findByUserId(Long userId);

    Optional<TeamMembership> findByTeamIdAndUserId(Long teamId, Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);

    @Query("""
            SELECT CASE WHEN COUNT(firstMembership) > 0 THEN true ELSE false END
            FROM TeamMembership firstMembership, TeamMembership secondMembership
            WHERE firstMembership.user.id = :firstUserId
              AND secondMembership.user.id = :secondUserId
              AND firstMembership.team.id = secondMembership.team.id
            """)
    boolean usersShareTeam(@Param("firstUserId") Long firstUserId,
                           @Param("secondUserId") Long secondUserId);
}
