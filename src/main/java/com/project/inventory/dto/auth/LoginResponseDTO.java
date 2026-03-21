package com.project.inventory.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String token; // Chứa chuỗi mã hóa loằng ngoằng
    // Bạn có thể thêm username hoặc role vào đây nếu Frontend cần hiển thị ngay
}