package org.example.project_java_service.model.dto.response;

import org.example.project_java_service.model.entity.enumeration.RoleEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private RoleEnum role;
    private boolean isActive;
}