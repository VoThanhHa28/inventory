package com.project.inventory.service.impl;

import com.project.inventory.dto.order.OrderRequestDTO;
import com.project.inventory.dto.order.OrderResponseDTO;
import com.project.inventory.entity.Order;
import com.project.inventory.entity.OrderDetail;
import com.project.inventory.entity.Product;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.repository.OrderRepository;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * @Transactional cực kỳ quan trọng ở đây.
     * Nếu khách mua 3 sản phẩm, sản phẩm thứ 3 bị hết hàng (văng Exception),
     * Transactional sẽ tự động Rollback (hoàn tác) lại toàn bộ thao tác trừ kho của 2 sản phẩm trước đó.
     * Đảm bảo tính ACID: Một là thành công tất cả, hai là không có gì thay đổi.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDTO placeOrder(OrderRequestDTO request) {
        log.info("Processing new order for customer: {}", request.getCustomerName());

        Order order = Order.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .orderDate(LocalDateTime.now())
                .status("COMPLETED")
                .build();

        double totalAmount = 0.0;

        // 1. TẠO LIST CHỨA KẾT QUẢ TRẢ VỀ
        List<OrderResponseDTO.OrderItemResponseDTO> responseItems = new java.util.ArrayList<>();

        for (OrderRequestDTO.OrderItemDTO itemDTO : request.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemDTO.getProductId()));

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

            // 2. GÓI DỮ LIỆU VÀO DTO ĐỂ TRẢ VỀ CHO FRONTEND
            responseItems.add(OrderResponseDTO.OrderItemResponseDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName()) // Giả sử Entity Product của bạn có hàm getName()
                    .quantity(itemDTO.getQuantity())
                    .unitPrice(product.getPrice())
                    .subTotal(itemTotal)
                    .build());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        log.info("Order successfully placed with ID: {}", savedOrder.getId());

        // 3. NHÉT MẢNG ITEMS VÀO KẾT QUẢ CUỐI CÙNG
        return OrderResponseDTO.builder()
                .orderId(savedOrder.getId())
                .customerName(savedOrder.getCustomerName())
                .totalAmount(savedOrder.getTotalAmount())
                .orderDate(savedOrder.getOrderDate())
                .status(savedOrder.getStatus())
                .message("Đặt hàng và trừ kho thành công!")
                .items(responseItems)
                .build();
    }
}