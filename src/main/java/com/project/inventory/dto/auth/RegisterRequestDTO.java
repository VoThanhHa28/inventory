package com.project.inventory.dto.auth;

import lombok.Data;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String fullName;
}
