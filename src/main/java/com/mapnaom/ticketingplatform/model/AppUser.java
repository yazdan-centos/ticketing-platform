package com.mapnaom.ticketingplatform.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
// Soft delete support to prevent removing users permanently (optional but recommended)
@SQLDelete(sql = "UPDATE app_users SET deleted = true WHERE id = ?")
public abstract class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true,  length = 50)
    private String username;
    private String password; // Encoded password (BCrypt)


    private String email;

    // Security roles backing authentication/authorization. Each role carries
    // its default permission set (see {@link Role}); per-user overrides live in
    // {@link UserPermissionGrant} / {@link UserResourceScope}.
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_roles",
            joinColumns = @JoinColumn(name = "app_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (roles == null) roles = new HashSet<>();
        if (deleted == null) deleted = false;
    }
    protected String getFullName(){
        return firstName + " " + lastName;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
