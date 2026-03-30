package com.project.inventory.repository;

import com.project.inventory.configuration.TestCacheConfig;
import com.project.inventory.entity.Order;
import com.project.inventory.entity.OrderDetail;
import com.project.inventory.entity.OrderStatus;
import com.project.inventory.entity.Product;
import com.project.inventory.entity.Role;
import com.project.inventory.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestCacheConfig.class)
@DisplayName("OrderRepository Integration Tests")
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;
    private User anotherUser;
    private Product testProduct;
    private Order pendingOrder;
    private Order completedOrder;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser = User.builder()
                .username("john_doe")
                .password("hashed_password_123")
                .fullName("John Doe")
                .role(Role.USER)
                .build();
        testUser = userRepository.save(testUser);

        anotherUser = User.builder()
                .username("jane_smith")
                .password("hashed_password_456")
                .fullName("Jane Smith")
                .role(Role.USER)
                .build();
        anotherUser = userRepository.save(anotherUser);

        // Create test product
        testProduct = Product.builder()
                .name("Samsung Galaxy S24")
                .description("Latest flagship phone")
                .price(899.99)
                .stockQuantity(100)
                .isDeleted(false)
                .build();
        testProduct = productRepository.save(testProduct);

        // Create test orders
        pendingOrder = Order.builder()
                .user(testUser)
                .totalAmount(899.99)
                .status(OrderStatus.PENDING)
                .build();
        pendingOrder = orderRepository.save(pendingOrder);

        completedOrder = Order.builder()
                .user(testUser)
                .totalAmount(1799.98)
                .status(OrderStatus.COMPLETED)
                .build();
        completedOrder = orderRepository.save(completedOrder);
    }

    @Test
    @DisplayName("Should create and find order by ID")
    void testCreateAndFindOrder() {
        Order order = Order.builder()
                .user(testUser)
                .totalAmount(499.99)
                .status(OrderStatus.PENDING)
                .build();

        Order saved = orderRepository.save(order);

        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals(testUser.getId(), saved.getUser().getId());
        assertEquals(499.99, saved.getTotalAmount());
        assertEquals(OrderStatus.PENDING, saved.getStatus());
    }

    @Test
    @DisplayName("Should find order by ID")
    void testFindById() {
        Optional<Order> result = orderRepository.findById(pendingOrder.getId());

        assertTrue(result.isPresent());
        assertEquals(pendingOrder.getId(), result.get().getId());
        assertEquals(testUser.getId(), result.get().getUser().getId());
    }

    @Test
    @DisplayName("Should return empty for non-existent order ID")
    void testFindById_NotFound() {
        Optional<Order> result = orderRepository.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all orders by user ID with pagination")
    void testFindByUserId_Paginated() {
        Page<Order> result = orderRepository.findByUserId(testUser.getId(), PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(o -> o.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("Should find paginated orders for specific user only")
    void testFindByUserId_Paginated_UserIsolation() {
        Page<Order> userOrders = orderRepository.findByUserId(testUser.getId(), PageRequest.of(0, 10));
        Page<Order> otherUserOrders = orderRepository.findByUserId(anotherUser.getId(), PageRequest.of(0, 10));

        assertEquals(2, userOrders.getTotalElements());
        assertEquals(0, otherUserOrders.getTotalElements());
    }

    @Test
    @DisplayName("Should find all orders by user ID without pagination")
    void testFindByUserId_List() {
        List<Order> result = orderRepository.findByUserId(testUser.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream()
                .allMatch(o -> o.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("Should return empty list for user with no orders")
    void testFindByUserId_Empty() {
        List<Order> result = orderRepository.findByUserId(anotherUser.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find orders by status PENDING")
    void testFindByStatus_Pending() {
        List<Order> result = orderRepository.findByStatus(OrderStatus.PENDING);

        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertEquals(1, result.stream()
                .filter(o -> o.getId().equals(pendingOrder.getId()))
                .count());
        assertTrue(result.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.PENDING));
    }

    @Test
    @DisplayName("Should find orders by status COMPLETED")
    void testFindByStatus_Completed() {
        List<Order> result = orderRepository.findByStatus(OrderStatus.COMPLETED);

        assertNotNull(result);
        assertTrue(result.stream()
                .allMatch(o -> o.getStatus() == OrderStatus.COMPLETED));
        assertTrue(result.stream()
                .anyMatch(o -> o.getId().equals(completedOrder.getId())));
    }

    @Test
    @DisplayName("Should return empty list for non-existent status")
    void testFindByStatus_NoResults() {
        List<Order> result = orderRepository.findByStatus(OrderStatus.CANCELLED);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find orders by user ID and status")
    void testFindByUserIdAndStatus() {
        List<Order> result = orderRepository.findByUserIdAndStatus(testUser.getId(), OrderStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(pendingOrder.getId(), result.get(0).getId());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    @DisplayName("Should return empty for user ID and status combination not found")
    void testFindByUserIdAndStatus_Empty() {
        List<Order> result = orderRepository.findByUserIdAndStatus(testUser.getId(), OrderStatus.CANCELLED);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should update order status atomically")
    void testUpdateOrderStatus() {
        int result = orderRepository.updateOrderStatus(pendingOrder.getId(), OrderStatus.COMPLETED);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, result);

        Optional<Order> updated = orderRepository.findById(pendingOrder.getId());
        assertTrue(updated.isPresent());
        assertEquals(OrderStatus.COMPLETED, updated.get().getStatus());
    }

    @Test
    @DisplayName("Should return 0 when updating non-existent order status")
    void testUpdateOrderStatus_NotFound() {
        int result = orderRepository.updateOrderStatus(999L, OrderStatus.CANCELLED);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should handle multiple status transitions")
    void testUpdateOrderStatus_MultipleTransitions() {
        // PENDING -> COMPLETED
        orderRepository.updateOrderStatus(pendingOrder.getId(), OrderStatus.COMPLETED);
        entityManager.flush();
        entityManager.clear();

        // COMPLETED -> CANCELLED
        int result = orderRepository.updateOrderStatus(pendingOrder.getId(), OrderStatus.CANCELLED);
        entityManager.flush();
        entityManager.clear();

        assertEquals(1, result);
        Optional<Order> final_state = orderRepository.findById(pendingOrder.getId());
        assertTrue(final_state.isPresent());
        assertEquals(OrderStatus.CANCELLED, final_state.get().getStatus());
    }

    @Test
    @DisplayName("Should cascade delete order details when order is deleted")
    void testCascadeDeleteOrderDetails() {
        // Create order with details
        Order order = Order.builder()
                .user(testUser)
                .totalAmount(899.99)
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        OrderDetail detail = OrderDetail.builder()
                .order(order)
                .product(testProduct)
                .quantity(2)
                .unitPrice(449.995)
                .build();
        // Save detail through order's cascade
        order.getOrderDetails().add(detail);
        orderRepository.save(order);
        entityManager.flush();

        Long orderId = order.getId();

        // Delete order
        orderRepository.deleteById(orderId);
        entityManager.flush();
        entityManager.clear();

        // Verify order is deleted
        Optional<Order> deletedOrder = orderRepository.findById(orderId);
        assertTrue(deletedOrder.isEmpty());
    }

    @Test
    @DisplayName("Should maintain order details relationship")
    void testOrderDetailsRelationship() {
        Order order = Order.builder()
                .user(testUser)
                .totalAmount(1799.98)
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        OrderDetail detail1 = OrderDetail.builder()
                .order(order)
                .product(testProduct)
                .quantity(2)
                .unitPrice(899.99)
                .build();

        order.getOrderDetails().add(detail1);
        orderRepository.save(order);
        entityManager.flush();
        entityManager.clear();

        Optional<Order> fetched = orderRepository.findById(order.getId());
        assertTrue(fetched.isPresent());
        assertEquals(1, fetched.get().getOrderDetails().size());
        assertEquals(testProduct.getId(), fetched.get().getOrderDetails().get(0).getProduct().getId());
    }

    @Test
    @DisplayName("Should update order with new details")
    void testUpdateOrderWithNewDetails() {
        Order order = Order.builder()
                .user(testUser)
                .totalAmount(899.99)
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);

        // Update total amount
        order.setTotalAmount(1299.99);
        order.setStatus(OrderStatus.COMPLETED);
        Order updated = orderRepository.save(order);

        assertEquals(1299.99, updated.getTotalAmount());
        assertEquals(OrderStatus.COMPLETED, updated.getStatus());
    }

    @Test
    @DisplayName("Should count orders by user")
    void testCountOrdersByUser() {
        long count = orderRepository.findByUserId(testUser.getId()).size();

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should verify order isolation between users")
    void testOrderIsolationBetweenUsers() {
        List<Order> userOrders = orderRepository.findByUserId(testUser.getId());
        List<Order> otherUserOrders = orderRepository.findByUserId(anotherUser.getId());

        assertEquals(2, userOrders.size());
        assertEquals(0, otherUserOrders.size());

        assertTrue(userOrders.stream()
                .noneMatch(o -> otherUserOrders.stream()
                        .anyMatch(op -> op.getId().equals(o.getId()))));
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testFindByUserId_PaginationBounds() {
        // Create multiple orders
        for (int i = 0; i < 15; i++) {
            Order order = Order.builder()
                    .user(testUser)
                    .totalAmount(100.0 + i)
                    .status(OrderStatus.PENDING)
                    .build();
            orderRepository.save(order);
        }

        Page<Order> page1 = orderRepository.findByUserId(testUser.getId(), PageRequest.of(0, 10));
        Page<Order> page2 = orderRepository.findByUserId(testUser.getId(), PageRequest.of(1, 10));

        assertTrue(page1.hasNext());
        assertTrue(page2.hasPrevious());
        assertEquals(17, page1.getTotalElements()); // 2 initial + 15 new
    }
}
