package com.project.inventory.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dashboard Statistics DTO
 * Contains key metrics for the dashboard display
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    
    /**
     * Total number of active products
     */
    private Long totalProducts;
    
    /**
     * Number of products with low stock (stock < threshold)
     */
    private Long lowStockCount;
    
    /**
     * Total sales revenue (sum of completed/confirmed orders)
     */
    private Double totalSales;
    
    /**
     * System efficiency percentage (0-100)
     * Can be calculated as percentage of products with sufficient stock
     */
    private Double efficiencyPercentage;
}
