package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.Meeting;
import com.mapnaom.ticketingplatform.model.enums.MeetingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Page<Meeting> findByTeamIdAndActiveTrue(Long teamId, Pageable pageable);

    Optional<Meeting> findByIdAndActiveTrue(Long id);

    List<Meeting> findByOrganizerIdAndActiveTrue(Long organizerId);

    @Query("""
            SELECT DISTINCT m FROM Meeting m JOIN m.participants p
            WHERE p.user.id = :userId AND m.active = true
            AND m.startTime BETWEEN :from AND :to
            ORDER BY m.startTime ASC
            """)
    List<Meeting> findUpcomingForUser(@Param("userId") Long userId,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(m) > 0 FROM Meeting m
            WHERE m.team.id = :teamId AND m.active = true AND m.status <> :cancelledStatus
            AND (:excludeId IS NULL OR m.id <> :excludeId)
            AND m.startTime < :endTime AND m.endTime > :startTime
            """)
    boolean existsOverlappingMeeting(@Param("teamId") Long teamId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("excludeId") Long excludeId,
                                     @Param("cancelledStatus") MeetingStatus cancelledStatus);
}
