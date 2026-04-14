package com.ticketflow.user_service.controller;

import com.ticketflow.user_service.dto.*;
import com.ticketflow.user_service.service.UserService;
import com.ticketflow.user_service.service.UserServiceImplementation;
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

public class UserController {

    private final UserService userService;

    public UserController(UserServiceImplementation userServiceImplementation){
        this.userService = userServiceImplementation;
    }

    // creer un user ...
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(user:create)")
    // creer un nouvel utilisateur ....
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest userRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userRequest));
    }

    // get un user par son id ...
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAuthority(user:read)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // assign Roles to User
    @PutMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority(user:manage-roles)")
    public ResponseEntity<UserResponse> assignRoleToUser(@PathVariable String id , @RequestBody Set<String> roleNames){
        return ResponseEntity.ok(userService.addRoleToUser(id , roleNames));
    }

    // creer un nouveau role
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority(user:manage-roles)")
    public ResponseEntity<RoleResponse> createRole(@RequestBody CreateRoleRequest roleRequest){
        return ResponseEntity.ok(userService.createRole(roleRequest));
    }
    // Lister tous les roles
    @GetMapping("/roles")
    @PreAuthorize("haAuthority(user:read)")
    public ResponseEntity<Set<RoleResponse>> getAllRoles(){
        return ResponseEntity.ok(userService.getAllRoles());
    }

    // Associer des permissions a un role
    @PutMapping("/{id}/roles/permissions")
    @PreAuthorize("hasAuthority(user:manage-roles)")
    public ResponseEntity<RoleResponse> assignPermissionsToRole(@PathVariable String id , @RequestBody Set<String> permissionNames){
        return ResponseEntity.ok(userService.addPermissionToRole(id,permissionNames));
    }

    // Lister toutes les permissions disponibles ...
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority(user:read)")
    public ResponseEntity<Set<PermissionResponse>> getAllPermissions(){
        return ResponseEntity.ok(userService.getAllPermissions());
    }



}
