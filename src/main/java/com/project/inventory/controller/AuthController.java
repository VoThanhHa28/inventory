package com.project.inventory.controller;

import com.project.inventory.dto.LoginRequestDTO;
import com.project.inventory.dto.LoginResponseDTO;
import com.project.inventory.dto.RegisterRequestDTO;
import com.project.inventory.dto.RegisterResponseDTO;
import com.project.inventory.dto.response.ApiResponse;
import com.project.inventory.entity.User;
import com.project.inventory.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(@RequestBody RegisterRequestDTO request) {
        // 1. Lấy dữ liệu từ Service
        RegisterResponseDTO data = authenticationService.register(request);

        // 2. Đóng gói vào hộp ApiResponse
        ApiResponse<RegisterResponseDTO> response = ApiResponse.<RegisterResponseDTO>builder()
                .code(201)
                .message("Đăng ký tài khoản thành công!")
                .data(data)
                .build();

        // 3. Trả về
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        // 1. Lấy dữ liệu từ Service
        LoginResponseDTO data = authenticationService.login(request);

        // 2. Đóng gói vào hộp ApiResponse
        ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                .code(200)
                .message("Đăng nhập thành công!")
                .data(data)
                .build();

        // 3. Trả về
        return ResponseEntity.ok(response);
    }
}
