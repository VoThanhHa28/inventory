package com.project.inventory.controller;

import com.project.inventory.dto.auth.LoginRequestDTO;
import com.project.inventory.dto.auth.LoginResponseDTO;
import com.project.inventory.dto.auth.RegisterRequestDTO;
import com.project.inventory.dto.auth.RegisterResponseDTO;
import com.project.inventory.dto.response.ApiResponse;
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
        // Call service to register user
        RegisterResponseDTO data = authenticationService.register(request);

        // Wrap response in ApiResponse
        ApiResponse<RegisterResponseDTO> response = ApiResponse.<RegisterResponseDTO>builder()
                .code(201)
                .message("User registered successfully!")
                .data(data)
                .build();

        // Return response
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        // Call service to authenticate user
        LoginResponseDTO data = authenticationService.login(request);

        // Wrap response in ApiResponse
        ApiResponse<LoginResponseDTO> response = ApiResponse.<LoginResponseDTO>builder()
                .code(200)
                .message("User logged in successfully!")
                .data(data)
                .build();

        // Return response
        return ResponseEntity.ok(response);
    }
}
