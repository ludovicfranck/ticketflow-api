package com.ticketflow.user_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class UserSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf((csrf -> csrf.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter()))
                );
        return http.build();
    }

    /**
     * Convertisseur JWT personnalise pour Keycloak
     */
    public Converter<Jwt,? extends AbstractAuthenticationToken> keycloakJwtConverter() {
        return new Converter<Jwt, AbstractAuthenticationToken>() {
            @Override
            public AbstractAuthenticationToken convert(Jwt jwt) {
                log.debug("[SECURITY] Conversion JWT - claims : {}" , jwt.getClaims().keySet());
                Set<GrantedAuthority> authorities = new HashSet<>();
                // 1.Extraction des permissions depuis le claim "authorities"
                List<String> authoritiesClaim = jwt.getClaimAsStringList("authorities");
                if (authoritiesClaim != null) {
                    authoritiesClaim.stream()
                            .map(simpleGrantedAuthorities -> new SimpleGrantedAuthority(simpleGrantedAuthorities))
                            .forEach(simpleGrantedAuthority -> authorities.add(simpleGrantedAuthority));
                }
//                // 2. Extraction des RÔLES (ex: ADMIN , AGENT ..) depuis le claim "roles"
//                List<String> rolesClaim = jwt.getClaimAsStringList("roles");
//                if (rolesClaim != null) {
//                    rolesClaim.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
//                }
//
//                // 3 Fallback : realm_access.authorities (permissions ou sous roles keycloak standard)
//                Map<String , Object> realmAccess = jwt.getClaimAsMap("realm_access");
//                if (realmAccess != null && realmAccess.containsKey("roles")){
//                    List<String> realmRoles = (List<String>) realmAccess.get("roles");
//                    realmRoles.forEach(role ->authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
//                }
//                log.debug("[SECURITY] Authorities extraites : {}" , authorities.stream()
//                        .map(grantedAuthority -> grantedAuthority.getAuthority())
//                        .collect(Collectors.joining(", ")));
                return new JwtAuthenticationToken(jwt, authorities);
            }
        };
    }

    /**
     * Convertisseur pour permettre les requetes a l'exterieur de l'environnement de Docker keycloak:8180 -> localhost:8180
     */
    @Bean
    public org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder() {
        String jwkSetUri = "http://localhost:8180/realms/ticketflow/protocol/openid-connect/certs";
        return org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
