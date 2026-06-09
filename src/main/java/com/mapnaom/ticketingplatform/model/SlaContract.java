package com.mapnaom.ticketingplatform.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


/**
 * Represents a Service Level Agreement (SLA) Contract entity that defines the terms of service
 * between a provider and a customer.
 *
 t* <p>Core functionality includes:
 * <ul>
 *   <li>Storing SLA details such as contract name, service scope, and response time commitments.</li>
 *   <li>Managing associations with a {@link Customer} (Many-to-One) and {@link Ticket}s (One-to-Many).</li>
 *   <li>Automatically tracking creation and update timestamps via JPA lifecycle callbacks.</li>
 * </ul>
 *
 * <p>Constructor Parameters:
 * <ul>
 *   <li>{@code id} (Long) - The unique primary key identifier, auto-generated.</li>
 *   <li>{@code contractName} (String) - The name of the SLA contract.</li>
 *   <li>{@code serviceScope} (String) - The scope of services covered by the contract.</li>
 *   <li>{@code responseTimeHours} (Integer) - The guaranteed response time in hours.</li>
 *   <li>{@code isActive} (Boolean) - Flag indicating if the contract is currently active (defaults to true).</li>
 *   <li>{@code createdAt} (LocalDateTime) - Timestamp of creation (managed by JPA).</li>
 *   <li>{@code updatedAt} (LocalDateTime) - Timestamp of the last update (managed by JPA).</li>
 *   <li>{@code customer} (Customer) - The associated customer entity.</li>
 *   <li>{@code tickets} (Set<Ticket>) - The set of tickets covered by this contract.</li>
 * </ul>
 *
 * <p>Usage Example:
 * <pre>
 * SlaContract contract = SlaContract.builder()
 *     .contractName("Premium Support")
 *     .serviceScope("24/7 Infrastructure")
 *     .responseTimeHours(2)
 *     .isActive(true)
 *     .customer(customerEntity)
 *     .build();
 * </pre>
 *
 * <p><b>Usage Restrictions &amp; Side Effects:</b>
 * <ul>
 *   <li>The {@code customer} relationship uses lazy fetching ({@code FetchType.LAZY}). Accessing it
 *       outside of an active Hibernate session will throw a {@code LazyInitializationException}.</li>
 *   <li>The {@code toString()} method excludes {@code customer} and {@code tickets} to prevent
 *       infinite recursion and lazy loading issues.</li>
 *   <li>The {@code equals} and {@code hashCode} implementations are based strictly on the entity's
 *       primary key. Avoid adding newly created (non-persisted, ID-null) entities to {@link java.util.Set}
 *       or {@link java.util.HashMap} before saving, as hash codes will change upon ID generation.</li>
 * </ul>
 */
@Entity
@Table(name = "sla_contracts")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String contractName;

    private String serviceScope;

    private Integer responseTimeHours;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    // Many contracts belong to one Customer. Owner side.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @ToString.Exclude
    private Customer customer;

    // One contract covers many Tickets. Inverse side.
    @OneToMany(mappedBy = "slaContract")
    @ToString.Exclude
    private Set<Ticket> tickets = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SlaContract that = (SlaContract) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
