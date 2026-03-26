package com.project.inventory.repository;

import com.project.inventory.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Atomically decrements the stock quantity of a product.
     * Utilizes database row-level locking to prevent race conditions during concurrent orders.
     *
     * @param productId The ID of the product to update.
     * @param quantity  The exact amount to deduct from current stock.
     * @return Number of rows affected. Returns 0 if product is not found or stock is insufficient.
     */
    @Modifying
    @Query(value = "UPDATE product SET stock_quantity = stock_quantity - :quantity " +
            "WHERE id = :productId AND stock_quantity >= :quantity", nativeQuery = true)
    int deductStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

}