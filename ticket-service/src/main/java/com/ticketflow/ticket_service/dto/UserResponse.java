package com.ticketflow.ticket_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String KeycloakId;
    private boolean enabled;
    private Set<String> roles;
    private Set<String> permissions;
}
