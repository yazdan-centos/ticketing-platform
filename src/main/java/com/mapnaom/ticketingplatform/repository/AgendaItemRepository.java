package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.AgendaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendaItemRepository extends JpaRepository<AgendaItem, Long> {
    List<AgendaItem> findByMeetingIdOrderByDisplayOrderAsc(Long meetingId);

    long countByMeetingId(Long meetingId);
}
