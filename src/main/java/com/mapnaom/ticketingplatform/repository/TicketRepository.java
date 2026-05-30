package com.mapnaom.ticketingmanagerserver.repository;

import com.mapnaom.ticketingmanagerserver.model.Ticket;
import com.mapnaom.ticketingmanagerserver.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCustomerId(Long id);

    List<Ticket> findByStatus(TicketStatus ticketStatus);

    List<Ticket> findByAssignedMemberId(Long id);


}