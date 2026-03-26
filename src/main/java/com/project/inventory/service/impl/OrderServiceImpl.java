package com.project.inventory.service.impl;

import com.project.inventory.dto.order.OrderRequestDTO;
import com.project.inventory.dto.order.OrderResponseDTO;
import com.project.inventory.entity.Order;
import com.project.inventory.entity.OrderDetail;
import com.project.inventory.entity.OrderStatus;
import com.project.inventory.entity.Product;
import com.project.inventory.entity.User;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.repository.InventoryRepository;
import com.project.inventory.repository.OrderRepository;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.repository.UserRepository;
import com.project.inventory.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    /**
     * Place order with automatic stock deduction (atomic transaction)
     * If any product has insufficient stock, entire transaction rolls back
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDTO placeOrder(OrderRequestDTO request, Long userId) {
        log.info("Processing new order for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .build();

        double totalAmount = 0.0;
        List<OrderResponseDTO.OrderItemResponseDTO> responseItems = new ArrayList<>();

        for (OrderRequestDTO.OrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findActiveById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

            // Atomic deduction from Product stock
            int rowsAffected = productRepository.deductStock(product.getId(), itemDTO.getQuantity());

            if (rowsAffected == 0) {
                log.error("Insufficient stock for product ID: {}", product.getId());
                throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ số lượng trong kho!");
            }

            double itemTotal = product.getPrice() * itemDTO.getQuantity();
            totalAmount += itemTotal;

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(product.getPrice())
                    .build();

            order.getOrderDetails().add(detail);

            responseItems.add(OrderResponseDTO.OrderItemResponseDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(product.getPrice())
                    .subTotal(itemTotal)
                    .build());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        log.info("Order successfully placed with ID: {} for user ID: {}", savedOrder.getId(), userId);

        return OrderResponseDTO.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUser().getId())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus().toString())
                .createdAt(savedOrder.getCreatedAt())
                .message("Đặt hàng thành công!")
                .items(responseItems)
                .build();
    }

    /**
     * Get order by ID with access control verification
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Verify user owns the order
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Order does not belong to user");
        }

        return mapOrderToResponse(order);
    }

    /**
     * Get all orders for current user with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::mapOrderToResponse);
    }

    /**
     * Update order status with inventory rollback logic
     * If cancelling order: restore inventory (add stock back)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDTO updateOrderStatus(Long orderId, String newStatusStr, Long adminUserId) {
        log.info("Admin {} attempting to update order {} status to {}", adminUserId, orderId, newStatusStr);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());

        // Validate status transition
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot update cancelled order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED && newStatus != OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot change completed order to " + newStatus);
        }

        // If cancelling: restore inventory
        if (newStatus == OrderStatus.CANCELLED) {
            for (OrderDetail item : order.getOrderDetails()) {
                int restored = productRepository.deductStock(item.getProduct().getId(), -item.getQuantity());
                if (restored == 0) {
                    log.warn("Failed to restore inventory for product {} in order {}", 
                            item.getProduct().getId(), orderId);
                }
            }
            log.info("Inventory restored for cancelled order {}", orderId);
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order {} status updated to {} by admin {}", orderId, newStatus, adminUserId);

        return mapOrderToResponse(updatedOrder);
    }

    /**
     * Helper method to convert Order entity to DTO
     */
    private OrderResponseDTO mapOrderToResponse(Order order) {
        List<OrderResponseDTO.OrderItemResponseDTO> items = new ArrayList<>();
        
        for (OrderDetail detail : order.getOrderDetails()) {
            items.add(OrderResponseDTO.OrderItemResponseDTO.builder()
                    .productId(detail.getProduct().getId())
                    .productName(detail.getProduct().getName())
                    .quantity(detail.getQuantity())
                    .unitPrice(detail.getUnitPrice())
                    .subTotal(detail.getUnitPrice() * detail.getQuantity())
                    .build());
        }

        return OrderResponseDTO.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().toString())
                .createdAt(order.getCreatedAt())
                .items(items)
                .build();
    }
}