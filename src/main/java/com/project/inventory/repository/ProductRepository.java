package com.project.inventory.repository;

import com.project.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ProductRepository - Handles product persistence
 * 
 * Implements soft delete filtering and atomic operations
 * All queries filter by is_deleted = false to exclude soft-deleted products
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find all non-deleted products with pagination
     * Used for list endpoint caching
     */
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    Page<Product> findAllActive(Pageable pageable);

    /**
     * Find all non-deleted products (no pagination)
     */
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    List<Product> findAllActive();

    /**
     * Find product by ID, excluding soft-deleted
     */
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Product> findActiveById(@Param("id") Long id);

    /**
     * Soft delete: Set is_deleted = true instead of removing row
     * @return Number of rows affected
     */
    @Modifying
    @Query("UPDATE Product SET isDeleted = true WHERE id = :id AND isDeleted = false")
    int softDeleteById(@Param("id") Long id);

    /**
     * Atomically decrements the stock quantity of a product
     * Prevents race conditions during concurrent orders
     *
     * @param productId The ID of the product to update
     * @param quantity  The exact amount to deduct from current stock
     * @return Number of rows affected. Returns 0 if product not found or stock insufficient
     */
    @Modifying
    @Query(value = "UPDATE product SET stock_quantity = stock_quantity - :quantity " +
            "WHERE id = :productId AND stock_quantity >= :quantity AND is_deleted = false", nativeQuery = true)
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Find all active products without filters
     * Used for in-memory filtering and caching
     */
    @Query("SELECT p FROM Product p WHERE p.isDeleted = false")
    List<Product> findAllActiveProducts();

}