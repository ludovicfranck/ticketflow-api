package com.ticketflow.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {
    @NotBlank(message = "Username must be fill")
    private String username;
    @NotBlank(message = "Email can't be blank")
    private String Email;
    private String firstName;
    private String lastName;
    // Mot de passe initial qui est transmis a Keycloak lors de la creation d'un User
    private String password;

}
