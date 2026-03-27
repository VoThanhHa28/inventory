package com.project.inventory.repository;

import com.project.inventory.entity.Order;
import com.project.inventory.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * OrderRepository - Handles order persistence and atomic operations
 * 
 * Supports user-based queries with pagination and status updates
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by ID (for verification and access control)
     */
    Optional<Order> findById(Long orderId);

    /**
     * Find all orders for a specific user with pagination
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all orders for a specific user without pagination
     */
    List<Order> findByUserId(Long userId);

    /**
     * Find all orders with a specific status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find all orders for a user with a specific status
     */
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    /**
     * Atomically update order status
     * @return Number of rows affected
     */
    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.status = :newStatus WHERE o.id = :orderId")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("newStatus") OrderStatus newStatus);
}
