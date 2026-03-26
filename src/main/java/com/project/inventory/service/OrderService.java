package com.project.inventory.service;

import com.project.inventory.dto.order.OrderRequestDTO;
import com.project.inventory.dto.order.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    
    /**
     * Place new order with automatic stock deduction
     */
    OrderResponseDTO placeOrder(OrderRequestDTO requestDTO, Long userId);
    
    /**
     * Get order details by ID (with access control)
     */
    OrderResponseDTO getOrderById(Long orderId, Long userId);
    
    /**
     * Get all orders for current user
     */
    Page<OrderResponseDTO> getUserOrders(Long userId, Pageable pageable);
    
    /**
     * Update order status (admin only)
     * Handles inventory rollback for cancelled orders
     */
    OrderResponseDTO updateOrderStatus(Long orderId, String newStatus, Long adminUserId);
}