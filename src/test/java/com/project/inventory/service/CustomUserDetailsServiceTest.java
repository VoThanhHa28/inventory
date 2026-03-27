package com.project.inventory.service;

import com.project.inventory.entity.Role;
import com.project.inventory.entity.User;
import com.project.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("john_doe")
                .password("encoded_password_123")
                .fullName("John Doe")
                .role(Role.USER)
                .build();
    }

    @Test
    void testLoadUserByUsername_UserFound_Success() {
        // Arrange
        when(userRepository.findByUsername("john_doe"))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("john_doe");

        // Assert
        assertNotNull(result);
        assertEquals("john_doe", result.getUsername());
        assertEquals("encoded_password_123", result.getPassword());
        assertTrue(result.isEnabled());
    }

    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent_user"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent_user");
        });
    }

    @Test
    void testLoadUserByUsername_UserWithAdminRole() {
        // Arrange
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByUsername("john_doe"))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("john_doe");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAuthorities());
        assertTrue(result.getAuthorities().size() > 0);
    }
}
