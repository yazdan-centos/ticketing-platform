package com.mapnaom.ticketingmanagerserver.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("CUSTOMER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer extends AppUser {

    // Specific Customer Fields
    private String companyName;
    private String phone;

    // Relationships
    // Customer has many SLA Contracts. Inverse side.
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SlaContract> slaContracts = new HashSet<>();

    // Customer creates many Tickets. Inverse side.
    @OneToMany(mappedBy = "customer")
    private Set<Ticket> tickets = new HashSet<>();
}
