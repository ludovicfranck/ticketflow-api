package com.ticketflow.user_service.dto;

import com.ticketflow.user_service.entity.Permission;
import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class RoleResponse {
    private String id;
    private String name;
    private String description;
    private Set<String> permissions;
}
