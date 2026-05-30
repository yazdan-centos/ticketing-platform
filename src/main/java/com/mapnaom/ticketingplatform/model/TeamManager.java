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
@DiscriminatorValue("MANAGER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamManager extends AppUser {

    // Specific Manager Fields
    private String department;

    // Relationships
    // One manager oversees many Team Members. Inverse side.
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TeamMember> teamMembers = new HashSet<>();
}
