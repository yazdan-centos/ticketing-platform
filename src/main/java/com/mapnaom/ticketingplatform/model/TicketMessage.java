package com.mapnaom.ticketingplatform.model;

import com.mapnaom.ticketingplatform.model.AppUser;
import com.mapnaom.ticketingplatform.model.Ticket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    // Using AppUser to allow Customer, TeamMember, or TeamManager to send messages
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private AppUser sender;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_by_customer")
    private boolean readByCustomer = false;

    @Column(name = "read_by_team")
    private boolean readByTeam = false;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}