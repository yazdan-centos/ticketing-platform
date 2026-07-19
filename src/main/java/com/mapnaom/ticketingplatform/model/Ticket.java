package com.mapnaom.ticketingplatform.model;

import com.mapnaom.ticketingplatform.model.enums.Priority;
import com.mapnaom.ticketingplatform.model.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketStatus status = TicketStatus.UNALLOCATED;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    // Whether this ticket is flagged as an emergency. This is an internal
    // triage attribute that is visible only to the assignee team member and
    // team managers – never to the customer who raised the ticket.
    @Column(name = "emergency", nullable = false)
    private boolean emergency = false;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    // Many tickets created by one Customer.
    // targetEntity ensures we link to the Customer subclass, not just AppUser.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    // Many tickets under one SLA Contract.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sla_contract_id")
    private SlaContract slaContract;

    // Many tickets assigned to one Team Member.
    // targetEntity ensures we link to the TeamMember subclass.
    @ManyToOne(fetch = FetchType.LAZY,targetEntity = TeamMember.class)
    @JoinColumn(name = "assigned_member_id")
    private TeamMember assignedMember;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketAttachment> ticketAttachments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketMessage> ticketMessages = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketStatusHistory> ticketStatusHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // Logic to set initial due date based on SLA could go here
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
