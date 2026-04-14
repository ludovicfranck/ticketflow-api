package com.ticketflow.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {
    // name of role || le nom du Role
    @NotBlank(message = "Can't be null")
    private String name;
    private String description;
}
