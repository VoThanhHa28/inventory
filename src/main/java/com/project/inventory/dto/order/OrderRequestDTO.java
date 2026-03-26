package com.project.inventory.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {

    @NotBlank(message = "Customer name is mandatory")
    private String customerName;

    @NotBlank(message = "Customer phone is mandatory")
    private String customerPhone;

    @NotEmpty(message = "Order must contain at least one product")
    @Valid
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Purchase quantity must be at least 1")
        private Integer quantity;
    }
}