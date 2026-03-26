package com.project.inventory.dto.order;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private String customerName;
    private Double totalAmount;
    private LocalDateTime orderDate;
    private String status;
    private String message;

    // Bổ sung danh sách chi tiết hóa đơn
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