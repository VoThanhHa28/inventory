package com.project.inventory.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // JWT secret key from application.properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // JWT token expiration time from application.properties
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // Generate JWT token with no extra claims
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Generate JWT token with extra claims
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims) // Add extra claims if provided
                .setSubject(userDetails.getUsername()) // Subject: username
                .setIssuedAt(new Date(System.currentTimeMillis())) // Issued at
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Expiration time
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Sign with HS256
                .compact(); // Compact to string
    }

    // Validate JWT token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Token is valid if username matches AND token is not expired
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Check if token is expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extract expiration date from token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract any claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey()) // Set signing key for verification
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Create signing key from BASE64-encoded secret
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}