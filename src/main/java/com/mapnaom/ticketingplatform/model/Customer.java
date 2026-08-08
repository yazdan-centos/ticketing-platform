package com.mapnaom.ticketingplatform.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@DiscriminatorValue("CUSTOMER")
@NamedQueries({
        @NamedQuery(name = "Customer.existsByUsernameAndEmail",
                query = "select (count(c) > 0) from Customer c " +
                        "where c.username = :username and c.email = :email")
})
@ToString
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Customer extends AppUser {

    // Specific Customer Fields
    private String companyName;
    private String phone;
    private String firstName;
    private String lastName;

    // Relationships
    // Customer has many SLA Contracts. Inverse side.
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<SlaContract> slaContracts = new HashSet<>();

    // Customer creates many Tickets. Inverse side.
    @OneToMany(mappedBy = "customer")
    @ToString.Exclude
    private Set<Ticket> tickets = new HashSet<>();

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Customer customer = (Customer) o;
        return getId() != null && Objects.equals(getId(), customer.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
