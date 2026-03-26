package com.project.inventory.dto.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * InventoryResponseDTO - Response payload for inventory operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponseDTO {

    private Long id;

    private Long productId;

    private String productName;

    private BigDecimal productPrice;

    private Integer stock;

    private Integer reserved;

    private Integer available;

    @JsonProperty("sold_count")
    private Integer soldCount;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
