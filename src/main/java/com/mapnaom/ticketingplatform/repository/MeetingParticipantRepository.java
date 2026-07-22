package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {
    List<MeetingParticipant> findByMeetingId(Long meetingId);

    Optional<MeetingParticipant> findByMeetingIdAndUserId(Long meetingId, Long userId);

    boolean existsByMeetingIdAndUserId(Long meetingId, Long userId);
}
