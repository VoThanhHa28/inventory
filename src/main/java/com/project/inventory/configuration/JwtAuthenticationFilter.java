package com.project.inventory.configuration;

import com.project.inventory.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // STEP 1: Extract JWT token from Authorization header
            // Format: "Authorization: Bearer <jwt_token>"
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;

            // If no Authorization header or wrong format, skip JWT processing
            // Request will be handled by Spring Security rules (permitAll/authenticated)
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Extract JWT token by removing "Bearer " prefix (7 characters)
            jwt = authHeader.substring(7);

            // STEP 2: Extract username from JWT token
            // This can fail if token is expired, malformed, or tampered with
            username = jwtService.extractUsername(jwt);

            // STEP 3: Validate token and set authentication in SecurityContext
            // Only process if:
            // - Username was successfully extracted from token
            // - User is not already authenticated (avoid re-authentication)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details from database to verify user exists
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validate JWT token signature and expiration date
                // Also checks if token matches the user's current credentials
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    log.debug("JWT token validated successfully for user: {}", username);

                    // Create authentication token representing the authenticated user
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,      // The authenticated user
                            null,             // No password needed (already validated)
                            userDetails.getAuthorities()  // User's roles/permissions
                    );

                    // Add request details (IP address, session ID, etc.)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Store authentication in SecurityContext so Spring knows this request is authenticated
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        } catch (ExpiredJwtException e) {
            // Token has expired - user should refresh their token
            // Log at DEBUG level (expected scenario, not an error)
            log.debug("JWT token expired for user {}: {}", 
                    e.getClaims() != null ? e.getClaims().getSubject() : "unknown", 
                    e.getMessage());

        } catch (JwtException e) {
            // Token is invalid, malformed, or signature doesn't match
            // Could indicate user tampering with token or using someone else's token
            // Log at WARN level (suspicious but not a system error)
            log.warn("Invalid JWT token detected: {}", e.getMessage());

        } catch (Exception e) {
            // Unexpected error (database down, user service error, etc.)
            // These are system-level issues that need attention
            // Log at ERROR level for debugging
            log.error("Unexpected error in JWT authentication filter", e);
        }

        // Continue filter chain
        // For public endpoints (permitAll), request proceeds normally
        // For protected endpoints, Spring Security will check authentication and reject if needed (401)
        filterChain.doFilter(request, response);
    }
}