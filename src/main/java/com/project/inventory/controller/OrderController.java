package com.project.inventory.controller;

import com.project.inventory.dto.response.ApiResponse;
import com.project.inventory.dto.order.OrderRequestDTO;
import com.project.inventory.dto.order.OrderResponseDTO;
import com.project.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Các API quản lý Đơn hàng")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Tạo đơn hàng mới", description = "Tạo đơn hàng và tự động trừ tồn kho")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(
            @Valid @RequestBody OrderRequestDTO requestDTO) {
        
        Long userId = getCurrentUserId();
        OrderResponseDTO response = orderService.placeOrder(requestDTO, userId);

        ApiResponse<OrderResponseDTO> apiResponse = ApiResponse.<OrderResponseDTO>builder()
                .code(HttpStatus.CREATED.value())
                .message("Đặt hàng thành công!")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Lấy chi tiết đơn hàng", description = "Lấy thông tin chi tiết của đơn hàng (chỉ chủ sở hữu hoặc admin)")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getOrderById(
            @PathVariable Long orderId) {
        
        Long userId = getCurrentUserId();
        OrderResponseDTO response = orderService.getOrderById(orderId, userId);

        ApiResponse<OrderResponseDTO> apiResponse = ApiResponse.<OrderResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy đơn hàng thành công!")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Danh sách đơn hàng của tôi", description = "Lấy tất cả đơn hàng của người dùng hiện tại")
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getUserOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponseDTO> response = orderService.getUserOrders(userId, pageable);

        ApiResponse<Page<OrderResponseDTO>> apiResponse = ApiResponse.<Page<OrderResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách đơn hàng thành công!")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Cập nhật trạng thái đơn hàng (chỉ admin) - tự động hoàn lại tồn kho nếu hủy")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        
        Long adminUserId = getCurrentUserId();
        OrderResponseDTO response = orderService.updateOrderStatus(orderId, status, adminUserId);

        ApiResponse<OrderResponseDTO> apiResponse = ApiResponse.<OrderResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật trạng thái đơn hàng thành công!")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Helper: Extract current user ID from JWT token
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        // Simplified: In production, extract from UserDetails or Claims
        // For now, assume principal is Long userId
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            // You may need to get userId from your custom UserDetails implementation
            throw new RuntimeException("Extract userId from JWT token");
        }
        return (Long) principal;
    }
}