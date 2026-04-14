package com.ticketflow.user_service.controller;

import com.ticketflow.user_service.dto.*;
import com.ticketflow.user_service.service.UserService;
import com.ticketflow.user_service.service.UserServiceImplementation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "UserController", description = "Gestion des utilisateurs, rôles et permissions")
public class UserController {

    private final UserService userService;
    // creer un user ...
    @Operation(summary = "Créer un utilisateur",
            description = "Crée l'utilisateur en BDD + Keycloak et publie user.created sur Kafka")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
            @ApiResponse(responseCode = "400", description = "Corps de la requête invalide"),
            @ApiResponse(responseCode = "403", description = "Scope user:create manquant")
    })
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(user:create)")
    // creer un nouvel utilisateur ....
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest userRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequest));
    }

    // get un user par son id ...
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority(user:read)")
    @Operation(summary = "Récupérer un utilisateur par ID",
            description = "Retourne l'utilisateur avec ses rôles et permissions effectives")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur inexistant"),
            @ApiResponse(responseCode = "403", description = "Scope user:read manquant")
    })
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // assign Roles to User
    @Operation(summary = "Attribuer des rôles à un utilisateur",
            description = "Met à jour les rôles en BDD et synchronise Keycloak. " +
                    "Le JWT suivant contiendra les nouvelles permissions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rôles attribués, Keycloak synchronisé"),
            @ApiResponse(responseCode = "403", description = "Scope user:manage-roles manquant"),
            @ApiResponse(responseCode = "404", description = "Utilisateur ou rôle introuvable")
    })
    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority(user:manage-roles)")
    public ResponseEntity<UserResponse> assignRoleToUser(@PathVariable String id , @RequestBody Set<String> roleNames){
        return ResponseEntity.ok(userService.addRoleToUser(id , roleNames));
    }

    // creer un nouveau role
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority(user:manage-roles)")
    @Operation(summary = "Créer un nouveau rôle",
            description = "Crée le rôle en BDD et dans Keycloak")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rôle créé"),
            @ApiResponse(responseCode = "403", description = "Scope user:manage-roles manquant")
    })
    public ResponseEntity<RoleResponse> createRole(@RequestBody @Valid CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createRole(request));
    }
    // Lister tous les roles
    @GetMapping("/roles")
    @PreAuthorize("haAuthority(user:read)")
    @Operation(summary = "Lister tous les rôles",
            description = "Retourne tous les rôles avec leurs permissions associées")
    public ResponseEntity<Set<RoleResponse>> getAllRoles(){
        return ResponseEntity.ok(userService.getAllRoles());
    }

    // Associer des permissions a un role
    @PutMapping("/{id}/roles/permissions")
    @PreAuthorize("hasAuthority(user:manage-roles)")
    @Operation(summary = "Associer des permissions à un rôle",
            description = "Remplace les permissions du rôle et synchronise Keycloak. " +
                    "Tous les utilisateurs portant ce rôle héritent des nouvelles permissions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions mises à jour"),
            @ApiResponse(responseCode = "403", description = "Scope user:manage-roles manquant"),
            @ApiResponse(responseCode = "404", description = "Rôle introuvable")
    })
    public ResponseEntity<RoleResponse> assignPermissionsToRole(@PathVariable String id , @RequestBody Set<String> permissionNames){
        return ResponseEntity.ok(userService.addPermissionToRole(id,permissionNames));
    }

    // Lister toutes les permissions disponibles ...
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority(user:read)")
    @Operation(summary = "Lister toutes les permissions disponibles")
    public ResponseEntity<Set<PermissionResponse>> getAllPermissions(){
        return ResponseEntity.ok(userService.getAllPermissions());
    }



}
