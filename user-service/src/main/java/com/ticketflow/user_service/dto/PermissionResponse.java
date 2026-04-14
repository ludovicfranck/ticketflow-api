package com.ticketflow.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {
    private String id;
    private String name;
    private String description;
}
