package com.project.inventory.repository;

import com.project.inventory.entity.ProductHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ProductHistoryRepository - Handles audit log persistence
 * 
 * Stores and retrieves product change history
 */
@Repository
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, Long> {

    /**
     * Find all history records for a specific product
     * Ordered by latest changes first
     */
    List<ProductHistory> findByProductIdOrderByChangedAtDesc(Long productId);
}
