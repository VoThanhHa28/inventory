package com.project.inventory.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * LoginResponseDTO - Response after successful login
 * Returns JWT token and user information
 */
@Data
@Builder
public class LoginResponseDTO {
    private String token;  // JWT token for authentication
    private UserDTO user;  // User info (id, username, role, etc.)
}