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

    // Lấy secret-key từ application.properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Lấy thời hạn token từ application.properties
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // --------------------------------------------------------
    // 1. MÁY IN THẺ (Tạo Token cho User)
    // --------------------------------------------------------
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims) // Thông tin phụ (nếu có)
                .setSubject(userDetails.getUsername()) // Tên chủ thẻ
                .setIssuedAt(new Date(System.currentTimeMillis())) // Ngày cấp
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Ngày hết hạn
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // Đóng dấu đỏ bảo mật
                .compact(); // Ép nhựa thành chuỗi String
    }

    // --------------------------------------------------------
    // 2. MÁY QUÉT THẺ (Kiểm tra thẻ thật/giả và tính hợp lệ)
    // --------------------------------------------------------
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Thẻ hợp lệ khi: Tên trên thẻ khớp với tên User truyền vào VÀ thẻ chưa hết hạn
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Lấy tên User (username) được in trên thẻ
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Kiểm tra thẻ đã hết hạn chưa?
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Đọc ngày hết hạn in trên thẻ
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // --------------------------------------------------------
    // CÁC HÀM HỖ TRỢ BÊN TRONG (Bóc tách dữ liệu thẻ)
    // --------------------------------------------------------
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey()) // Đưa "Chữ ký bí mật" vào để đối chiếu
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Tạo chìa khóa (Key) chuẩn hóa từ chuỗi secretKey
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}