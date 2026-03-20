package com.project.inventory.config;

import com.project.inventory.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService; // Lấy hàm tìm user trong DB

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Lấy cái vé từ Header của HTTP Request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Kiểm tra: Nếu không có vé, hoặc vé không bắt đầu bằng chữ "Bearer " -> Đuổi đi luôn (Cho qua trạm nhưng không được cấp quyền)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Cắt bỏ chữ "Bearer " (7 ký tự) để lấy cái chuỗi mã hóa loằng ngoằng
        jwt = authHeader.substring(7);

        // 4. Bỏ vé vào máy quét để lấy tên User
        username = jwtService.extractUsername(jwt);

        // 5. Nếu quét được tên, VÀ người này chưa được xác thực trong phiên làm việc hiện tại
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Tìm user trong DB xem có thật không
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 6. Nhờ máy quét kiểm tra vé có hết hạn/đúng chủ không?
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 7. Vé CHUẨN -> Cấp thẻ xanh (Authentication) cho phép đi vào hệ thống
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Lưu trạng thái "Đã đăng nhập" vào Context của Spring
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 8. Chuyển cho trạm kiểm soát tiếp theo
        filterChain.doFilter(request, response);
    }
}