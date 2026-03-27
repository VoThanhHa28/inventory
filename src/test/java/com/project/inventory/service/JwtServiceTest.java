package com.project.inventory.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUser;
    private String secretKey = "9a4f2c413d7b596375386d78397a244226452948404d635166546a576e5a7234743777217a25432a462d2a612e344525f9";
    private long jwtExpiration = 86400000; // 24 hours in milliseconds

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Set private fields using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
        
        // Create test user
        testUser = User.builder()
                .username("testuser")
                .password("password123")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void testGenerateToken_Success() {
        // Arrange & Act
        String token = jwtService.generateToken(testUser);
        
        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.contains(".")); // JWT format: header.payload.signature
    }

    @Test
    void testIsTokenValid_WithValidToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        
        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);
        
        // Assert
        assertTrue(isValid);
    }

    @Test
    void testExtractUsername_Success() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        
        // Act
        String username = jwtService.extractUsername(token);
        
        // Assert
        assertEquals("testuser", username);
    }
}
