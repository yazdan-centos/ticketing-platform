package com.mapnaom.ticketingplatform.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "user_resource_scopes")
@Getter @Setter @NoArgsConstructor
public class UserResourceScope {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private User user;
    private String resourceType;                     // "TICKET"
    @Enumerated(EnumType.STRING) private AccessScope scope;
}


