package com.mapnaom.ticketingplatform.model;

import com.mapnaom.ticketingplatform.model.enums.GrantEffect;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing the granting of permissions to users.
 * This class maps to the "user_permission_grants" table in the database.
 * It contains information about which user has been granted which permission and the effect of that grant.
 */
@Entity
@Table(name = "user_permission_grants")
@Getter
@Setter
@NoArgsConstructor
public class UserPermissionGrant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private AppUser user;
    @ManyToOne(optional = false) private Permission permission;
    @Enumerated(EnumType.STRING) private GrantEffect effect;  // ALLOW adds, DENY removes
}

