package com.ticketflow.user_service.service;

import com.ticketflow.user_service.dto.*;
import com.ticketflow.user_service.entity.Permission;
import com.ticketflow.user_service.entity.Role;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public interface UserService {

    // methodes lies au User
    UserResponse createUser(CreateUserRequest userRequest);
    UserResponse getUserById(String id);
    UserResponse addRoleToUser(String id_User,Set<String> roleNames);

    // --------- methodes pour les roles
    // creation d'un role
    RoleResponse createRole(CreateRoleRequest roleRequest);
    //Listing de tous les roles disponibles
    Set<RoleResponse> getAllRoles();
    // Associer une ou des permissions a un role
    RoleResponse addPermissionToRole(String id_Role ,Set<String> permissionNames);
    // Listing de toutes les permissions disponibles ...
    Set<PermissionResponse> getAllPermissions();




}
