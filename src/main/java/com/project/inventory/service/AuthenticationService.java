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
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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

    public LoginResponseDTO login(LoginRequestDTO request) {
        // Authenticate credentials using AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Load user from database after successful authentication
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        // Generate JWT token for authenticated user
        var jwtToken = jwtService.generateToken(user);

        // Return token in response DTO
        return LoginResponseDTO.builder()
                .token(jwtToken)
                .build();
    }
}