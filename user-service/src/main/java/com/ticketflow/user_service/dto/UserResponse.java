package com.ticketflow.user_service.dto;


import com.ticketflow.user_service.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
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
