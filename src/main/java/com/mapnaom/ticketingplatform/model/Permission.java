package com.mapnaom.ticketingplatform.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "permissions")
@Getter @Setter @NoArgsConstructor
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;          // e.g. "TICKET_READ", "TICKET_DELETE"
    private String description;
}

