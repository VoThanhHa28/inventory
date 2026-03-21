package com.project.inventory.service;

import com.project.inventory.dto.auth.LoginRequestDTO;
import com.project.inventory.dto.auth.LoginResponseDTO;
import com.project.inventory.dto.auth.RegisterRequestDTO;
import com.project.inventory.dto.auth.RegisterResponseDTO;
import com.project.inventory.entity.Role;
import com.project.inventory.entity.User;
import com.project.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // --- THÊM 2 ÔNG NÀY VÀO ---
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // (Hàm register cũ giữ nguyên)
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        var user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);
        return RegisterResponseDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .fullName(savedUser.getFullName())
                .build();
    }

    // --- THÊM HÀM LOGIN MỚI ---
    public LoginResponseDTO login(LoginRequestDTO request) {
        // 1. Nhờ Vệ sĩ kiểm tra User/Pass. Nếu sai, nó sẽ ném lỗi 403/BadCredentials ngay tại dòng này.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Nếu đi qua được dòng trên -> Pass đúng! Ta tìm User đó trong DB ra.
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        // 3. Đưa User vào máy in thẻ để ép ra chuỗi Token
        var jwtToken = jwtService.generateToken(user);

        // 4. Đóng gói Token vào DTO trả về cho Frontend
        return LoginResponseDTO.builder()
                .token(jwtToken)
                .build();
    }
}