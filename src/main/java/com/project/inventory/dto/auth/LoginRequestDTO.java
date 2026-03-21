package com.project.inventory.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String username;
    private String password;
}
