package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.MeetingNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingNoteRepository extends JpaRepository<MeetingNote, Long> {
    List<MeetingNote> findByMeetingIdOrderByCreatedAtAsc(Long meetingId);
}
