package com.project.inventory.service;

import com.project.inventory.dto.dashboard.ActivityItemDto;
import com.project.inventory.dto.dashboard.DashboardStatsDto;
import com.project.inventory.entity.*;
import com.project.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardService
 * Aggregates data for dashboard display
 * - Calculates KPIs (total products, low stock alerts, total sales)
 * - Fetches recent activity logs
 * - Computes system efficiency metrics
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class DashboardService {
    
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final ProductHistoryRepository productHistoryRepository;
    
    @Value("${inventory.low-stock-threshold:10}")
    private Integer lowStockThreshold;
    
    /**
     * Get dashboard statistics
     * - Total active products
     * - Products with low stock (stock < threshold)
     * - Total sales revenue
     * - System efficiency percentage
     */
    public DashboardStatsDto getStats() {
        log.debug("Calculating dashboard stats with low-stock-threshold={}", lowStockThreshold);
        
        // 1. Count total active products (not deleted)
        Long totalProducts = productRepository.count();
        
        // 2. Count low stock items (inventory.stock < threshold)
        Long lowStockCount = inventoryRepository.findAll().stream()
            .filter(inv -> inv.getStock() < lowStockThreshold)
            .count();
        
        // 3. Calculate total sales (sum of totalAmount from completed orders)
        Double totalSales = orderRepository.findAll().stream()
            .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        // 4. Calculate efficiency percentage
        // Efficiency = (products with sufficient stock / total products) * 100
        Double efficiencyPercentage = totalProducts > 0 
            ? ((totalProducts - lowStockCount) / (double) totalProducts) * 100.0
            : 100.0;
        
        log.debug("Dashboard stats: totalProducts={}, lowStockCount={}, totalSales={}, efficiency={}%",
            totalProducts, lowStockCount, totalSales, String.format("%.2f", efficiencyPercentage));
        
        return DashboardStatsDto.builder()
            .totalProducts(totalProducts)
            .lowStockCount(lowStockCount)
            .totalSales(totalSales)
            .efficiencyPercentage(efficiencyPercentage)
            .build();
    }
    
    /**
     * Get recent activity logs for dashboard
     * Returns last N activities across all products
     * 
     * @param limit Maximum number of activities to return (default: 10)
     * @return List of recent activities with DTOs
     */
    public List<ActivityItemDto> getRecentActivity(Integer limit) {
        if (limit == null || limit < 1) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100; // Cap at 100 to prevent abuse
        }
        
        log.debug("Fetching {} recent activities", limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<ProductHistory> activities = productHistoryRepository.findAllByOrderByChangedAtDesc(pageable);
        
        return activities.stream()
            .map(history -> ActivityItemDto.builder()
                .productId(history.getProductId())
                .productName(history.getName())
                .action(history.getAction())
                .changedBy(history.getChangedBy())
                .changedAt(history.getChangedAt())
                .build())
            .collect(Collectors.toList());
    }
}
