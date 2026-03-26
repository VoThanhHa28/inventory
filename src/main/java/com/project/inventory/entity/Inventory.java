package com.project.inventory.entity;

import com.project.inventory.entity.BaseEntity;
import com.project.inventory.entity.Product;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inventory Entity - Manages product stock, reservations, and sales
 * 
 * Fields:
 * - product: One-to-One relationship with Product
 * - stock: Available stock quantity (min 0)
 * - reserved: Quantity reserved for orders (min 0)
 * - soldCount: Total quantity sold (min 0)
 * - status: Current inventory status (AVAILABLE, RESERVED, SOLD)
 */
@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Min(value = 0, message = "Stock must be >= 0")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer stock = 0;

    @Min(value = 0, message = "Reserved must be >= 0")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer reserved = 0;

    @Min(value = 0, message = "Sold count must be >= 0")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer soldCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(50) DEFAULT 'AVAILABLE'")
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    /**
     * Get available quantity = stock - reserved
     */
    public Integer getAvailable() {
        return stock - reserved;
    }

    /**
     * Check if sufficient stock is available
     */
    public boolean hasSufficientStock(Integer quantity) {
        return getAvailable() >= quantity;
    }
}
