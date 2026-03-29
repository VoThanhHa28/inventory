package com.project.inventory.dto.auth;

import com.project.inventory.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * UserDTO - User data transfer object for API responses
 * Contains user information without sensitive data (no password)
 */
@Data
@Builder
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
