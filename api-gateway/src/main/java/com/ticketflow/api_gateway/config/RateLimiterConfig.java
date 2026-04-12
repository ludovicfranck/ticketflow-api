package com.ticketflow.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.function.Function;

@Configuration
public class RateLimiterConfig {
    @Bean
    public Function<ServerRequest, String> userKeyResolver(){
        return request -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // On limite par le "preferred_username" ou le "sub" du token
            // on assigne le anonymous si le auth n'est pas authentifie et il est non null (donc il contient une requete)
            return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
        };
    }
}
