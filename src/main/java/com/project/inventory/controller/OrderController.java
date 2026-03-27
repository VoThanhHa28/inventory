package com.project.inventory.controller;

import com.project.inventory.entity.User;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

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
    @Cacheable(
        value = "userOrders",
        key = "T(java.lang.String).format('my-orders:%d:page:%d:size:%d', " +
              "@T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getPrincipal().id, " +
              "#page, #size)"
    )
    @Operation(summary = "Danh sách đơn hàng của tôi", description = "Lấy tất cả đơn hàng của người dùng hiện tại (cached 60s)")
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
    @CacheEvict(value = "userOrders", allEntries = true)  // Clear all user order caches on status update
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
     * Helper: Extract current user ID from SecurityContext.
     * Principal is set by JwtAuthenticationFilter as the User entity (implements UserDetails).
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}