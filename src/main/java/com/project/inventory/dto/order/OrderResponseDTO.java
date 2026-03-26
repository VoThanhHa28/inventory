package com.project.inventory.dto.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private Long userId;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private String status;
    private String message;

    // Danh sách chi tiết sản phẩm trong đơn hàng
    private List<OrderItemResponseDTO> items;

    @Data
    @Builder
    public static class OrderItemResponseDTO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double unitPrice;
        private Double subTotal;
    }
}