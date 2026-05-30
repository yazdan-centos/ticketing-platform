package com.mapnaom.ticketingplatform.repository;

import com.mapnaom.ticketingplatform.model.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
}