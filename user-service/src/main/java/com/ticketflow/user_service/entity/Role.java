package com.ticketflow.user_service.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;



@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity @Table(name = "roles")
public class Role {
    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private String id;
    @Column(unique = true , nullable = false)
    private String name; // "ADMIN" , "AGENT" , "USER"
    private String description;
    /**
     * RBAC = Role Base Access Control
     * L'union des permissions de tous les roles d'un utilisateur .
     * forme ses droits effectifs
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions" ,
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Cette methode va permettre de retourner les noms des permissions pour ce role
     */
    public Set<String> getPermissionNames(){
        return  permissions.stream()
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());
    }

}
