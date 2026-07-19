package com.mapnaom.ticketingplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_messages")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    @ToString.Exclude
    private Ticket ticket;

    // Using AppUser to allow Customer, TeamMember, or TeamManager to send messages
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @ToString.Exclude
    private AppUser sender;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "read_by_customer")
    private boolean readByCustomer = false;

    @Column(name = "read_by_team")
    private boolean readByTeam = false;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TicketMessage other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}