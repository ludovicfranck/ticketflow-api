package com.ticketflow.user_service.keycloak;


import com.ticketflow.user_service.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeyCloakSyncService {
    private final Keycloak keycloak;
    private final String RealmName = "ticketflow";

    public void createKeycloakUser(User user , String password){
        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setUsername(user.getUsername());
        kcUser.setEmail(user.getEmail());
        kcUser.setEnabled(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(password);
        cred.setTemporary(false);
        kcUser.setCredentials(Collections.singletonList(cred));

        keycloak.realm(RealmName).users().create(kcUser);
    }
    public void updateUserRolesInKeycloak(String username , Set<String> roleNames){
        // 1 Recuperation de l'ID de l'utilisateur dans Keycloak
        String userId = keycloak.realm(RealmName).users().search(username).get(0).getId();

        // 2 Recuperation des representations des roles
        List<RoleRepresentation> rolesToAdd = roleNames.stream()
                .map(name -> keycloak.realm(RealmName).roles().get(name).toRepresentation())
                .toList();

        // 3 Assignation des roles (gestion automatique des unions par Keycloak)
        keycloak.realm(RealmName).users().get(userId).roles().realmLevel().add(rolesToAdd);
    }

    public void createRoleInKeycloak(@NotBlank(message = "Can't be null") String roleName) {
        try{
            RoleRepresentation roleRepresentation = new RoleRepresentation();
            roleRepresentation.setName(roleName);
            roleRepresentation.setDescription("Rôle synchronisé depuis l'application Spring Boot user-service");

            // creation du role dans le Realm
            keycloak.realm(RealmName).roles().create(roleRepresentation);
            log.info("Role {} cree avec succes dans KeyCloak dans le realm {} " , roleName , RealmName);
        } catch (Exception e) {
            log.error("Erreur lors de la creation du Role {} dans Keycloak" , roleName, e);
        }
    }
    // Cette methode permet d'assigner des permissions a un role ...
    public void updateRolePermissionsInKeyCloak(String roleName , Set<String> permissionsNames){
        try{
            // Recuperation du role parent
            RoleResource roleParent = keycloak.realm(RealmName).roles().get(roleName);
            // Transformation du nom des permissions en liste de Representation En supposant que les permissions ou Roles  Composites sont deja crees dans Keycloak
            List<RoleRepresentation> compositesToAdd = permissionsNames.stream() // compositesToAdd = ensemble des permissions
                    .map(permissionName -> {
                        try{
                            return keycloak.realm(RealmName).roles().get(permissionName).toRepresentation();
                        } catch (Exception e) {
                            log.error("La permission {} n'existe pas dans KeyCloak" , permissionName);
                            return null;
                        }
                    })
                    .filter(role -> role!= null)
                    .collect(Collectors.toList());
            // Ajout des roles composites ou permissions au role Parent
            if (!compositesToAdd.isEmpty()){
                roleParent.addComposites(compositesToAdd);
            }
        }
        catch (Exception exception){
            log.error("[KEYCLOAK] Erreur lors de la creation du role composite pour {}", roleName);
            throw new RuntimeException("Erreur synchronisation Keycloak");
        }

    }
}
