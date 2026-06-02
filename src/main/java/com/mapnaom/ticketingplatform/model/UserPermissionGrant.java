package com.mapnaom.ticketingplatform.model;

import com.mapnaom.ticketingplatform.model.enums.GrantEffect;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_permission_grants")
@Getter
@Setter
@NoArgsConstructor
public class UserPermissionGrant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private User user;
    @ManyToOne(optional = false) private Permission permission;
    @Enumerated(EnumType.STRING) private GrantEffect effect;  // ALLOW adds, DENY removes
}

