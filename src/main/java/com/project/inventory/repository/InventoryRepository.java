package com.project.inventory.repository;

import com.project.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * InventoryRepository - Handles atomic inventory operations using native SQL
 * 
 * All modifying operations are atomic at database level to prevent race conditions
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by product ID
     */
    Optional<Inventory> findByProductId(Long productId);

    /**
     * Atomically add stock
     * @return 1 if successful, 0 if product not found
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE inventory SET stock = stock + :quantity WHERE product_id = :productId", nativeQuery = true)
    int addStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Atomically remove stock (only if sufficient stock available)
     * @return 1 if successful, 0 if insufficient stock or product not found
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE inventory SET stock = stock - :quantity WHERE product_id = :productId AND stock >= :quantity", nativeQuery = true)
    int removeStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Atomically reserve stock (decrease stock, increase reserved)
     * @return 1 if successful, 0 if insufficient stock or product not found
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE inventory SET stock = stock - :quantity, reserved = reserved + :quantity WHERE product_id = :productId AND stock >= :quantity", nativeQuery = true)
    int reserve(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Atomically mark as sold (decrease reserved, increase soldCount)
     * @return 1 if successful, 0 if insufficient reserved or product not found
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE inventory SET reserved = reserved - :quantity, sold_count = sold_count + :quantity WHERE product_id = :productId AND reserved >= :quantity", nativeQuery = true)
    int markAsSold(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Atomically release reserved stock (decrease reserved, increase stock)
     * @return 1 if successful, 0 if insufficient reserved or product not found
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE inventory SET reserved = reserved - :quantity, stock = stock + :quantity WHERE product_id = :productId AND reserved >= :quantity", nativeQuery = true)
    int releaseReserved(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}
