package com.ticketflow.user_service.config;


import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl("http://keycloak:8180") // L'URL de ton Keycloak Docker
                .realm("master")                    // Toujours "master" pour l'admin
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli")
                .username("admin")                  // Ton login Keycloak
                .password("admin")                  // Ton mot de passe Keycloak
                .build();
    }
}
