package com.ticketflow.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(unique = true , nullable = false)
    private String username; // le nom d'utilisateur
    @Column(unique = true , nullable = false)
    private String email;
    private String firstName;
    private String lastName;
    /**
     * Id de l'utilisateur cote Keycloak pour la synchronisation
     */
    private String KeycloakId;
    private boolean enabled = true;
    /**
     * RBAC : Un User peut avoir plusieurs roles .
     * ses permissions = Union de toutes les permissions de ses roles
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    /**
     * ABAC : calcule l'union de toutes les permissions des roles du User
     * c'est ce set (evite les doublons de permissions) qui sera synchronise dans Keycloak et injecte dans le JWT
     */
    public Set<String> getEffectivePermissions(){
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());
    }
}
