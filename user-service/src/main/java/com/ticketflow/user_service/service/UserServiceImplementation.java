package com.ticketflow.user_service.service;

import com.ticketflow.user_service.dto.*;
import com.ticketflow.user_service.entity.Permission;
import com.ticketflow.user_service.entity.Role;
import com.ticketflow.user_service.entity.User;
import com.ticketflow.user_service.kafka.UserEventProducer;
import com.ticketflow.user_service.keycloak.KeyCloakSyncService;
import com.ticketflow.user_service.repository.PermissionRepositoty;
import com.ticketflow.user_service.repository.RoleRepositoty;
import com.ticketflow.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements UserService{

    private final UserRepository userRepository;
    private final KeyCloakSyncService keyCloakSyncService;
    private final PermissionRepositoty permissionRepository;
    private final RoleRepositoty roleRepository;
    private final UserEventProducer userEventProducer;

    /*
    * Cette methode permet de creer un user
    */
    @Override
    public UserResponse createUser(CreateUserRequest userRequest) {
       // Etape 1 : Sauvegarde en PostgreSQL
        User user = User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .firstName(userRequest.getFirstName())
                .lastName(userRequest.getLastName())
                .build();
        user = userRepository.save(user);
        // Etape 2 : Synchronisation Keycloak
        keyCloakSyncService.createKeycloakUser(user, userRequest.getPassword());

        // Etape 3 : Notification asynchrone via Kafka
        UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(userRequest.getEmail())
                .build();

        userEventProducer.sendUserCreatedEvent(userCreatedEvent);
        return mapToUserResponse(user);

    }

    /*
    * Cette methode agit comme un mapper en transformant User -> UserResponse
    */
    public UserResponse mapToUserResponse(User user) {
        // Extraction du nom des roles
//        Set<String> roles = user.getRoles().stream()
//                .map(role -> role.getName())
//                .collect(Collectors.toSet());
//        // Extraction du nom des permissions
//        Set<String> permissions = user.getEffectivePermissions();
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
//                .roles(roles)
//                .permissions(permissions)
                .build();
    }

    /*
    * Cette methode permet de recuperer un User par son id
    */
    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve !"));
        return mapToUserResponse(user);
    }

    /*
        * Cette methode permet d'assigner des roles a un User
     */
    @Override
    public UserResponse addRoleToUser(String id_User, Set<String> roleNames) {
        User user = userRepository.findById(id_User).orElseThrow(()-> new ResourceNotFoundException("Utilisateur non trouve !"));
        Set<Role> roles = new HashSet<>(roleRepository.findByNameIn(roleNames));

        user.setRoles(roles);
        userRepository.save(user);

        // synchronisation keycloak
        keyCloakSyncService.updateUserRolesInKeycloak(user.getUsername(),roleNames);
        return mapToUserResponse(user);
    }

    @Override
    public RoleResponse createRole(CreateRoleRequest roleRequest) {
        Role role = Role.builder()
                .name(roleRequest.getName())
                .build();
        role = roleRepository.save(role);

        // SYNC KEYCLOAK : Créer le rôle dans le Realm
        keyCloakSyncService.createRoleInKeycloak(roleRequest.getName());

        return mapToRoleResponse(role);
    }
    /**
     * Cette methode transforme un Role -> RoleResponse ...
     */
    public RoleResponse mapToRoleResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissionNames())
                .build();
    }


    /**
     * Cette methode permet de lister tous les roles disponibles ...
     */
    @Override
    public Set<RoleResponse> getAllRoles() {
        List<Role> rolesDoublons = roleRepository.findAll();
        Set<Role> roles = new HashSet<>(rolesDoublons);
        return roles.stream()
        .map(role -> mapToRoleResponse(role))
        .collect(Collectors.toSet());
    }

    /**
     * Cette methode permet d'assigner des permissions a un role
     * @param id_Role
     * @param permissionNames
     * @return
     */
    @Override
    public RoleResponse addPermissionToRole(String id_Role, Set<String> permissionNames) {
        Role role = roleRepository.findById(id_Role)
                .orElseThrow(() -> new ResourceNotFoundException("Le Role n'a pas ete retrouve !"));
        // recuperer la ou les permissions a assigner ...
        Set<Permission> permissions = new HashSet<>(permissionRepository.findByNameIn(permissionNames));
        role.setPermissions(permissions);
        roleRepository.save(role);
        keyCloakSyncService.updateRolePermissionsInKeyCloak(role.getName() , permissionNames);

        return mapToRoleResponse(role);
    }

    /**
     * Cette methode permet de lister toutes les permissions disponibles
     */
    @Override
    public Set<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(permission -> PermissionResponse.builder()
                        .id(permission.getId())
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .build()
                )
                .collect(Collectors.toSet()); // Set car cela evite de retourner des doublons ...
    }
}
