package com.mapnaom.ticketingplatform.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a system permission entity mapped to the "permissions" database table.
 * <p>
 * Core Functionality:
 * - Maps permission data to the underlying relational database using JPA.
 * - Uniquely identifies permissions by a specific code (e.g., "TICKET_READ", "TICKET_DELETE").
 * - Provides a human-readable description for the permission.
 * </p>
 * <p>
 * Constructor Parameters:
 * - No-args constructor: Provided by the {@code @NoArgsConstructor} annotation,
 *   required by JPA/Hibernate for entity instantiation.
 * </p>
 * <p>
 * Usage Example:
 * <pre>
 * // Creating a new permission instance
 * Permission permission = new Permission();
 * permission.setCode("TICKET_READ");
 * permission.setDescription("Allows reading ticket data");
 * </pre>
 * </p>
 * <p>
 * Usage Constraints &amp; Side Effects:
 * - The {@code code} field must be unique and cannot be null. Attempting to persist
 *   an entity with a duplicate or null code will result in a database constraint violation.
 * - Getters and setters are automatically generated at compile time via Lombok annotations.
 * </p>
 *///t
//r
//n
@Entity @Table(name = "permissions")
@Getter @Setter @NoArgsConstructor
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;          // e.g. "TICKET_READ", "TICKET_DELETE"
    private String description;
}

