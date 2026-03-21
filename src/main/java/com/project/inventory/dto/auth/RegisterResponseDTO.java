package com.project.inventory.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponseDTO {
    private Long id;
    private String username;
    private String fullName;
    // Tuyệt đối KHÔNG có field password ở đây
}