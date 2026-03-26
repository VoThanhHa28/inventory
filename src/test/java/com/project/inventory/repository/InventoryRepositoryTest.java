package com.project.inventory.repository;

import com.project.inventory.entity.Inventory;
import com.project.inventory.entity.InventoryStatus;
import com.project.inventory.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for InventoryRepository
 * 
 * Test scenarios:
 * - ADD_STOCK: increase available stock using native SQL
 * - REMOVE_STOCK: decrease available stock
 * - RESERVE: move from available to reserved
 * - MARK_AS_SOLD: move from reserved to sold count
 * - RELEASE_RESERVED: return from reserved to available
 * - FIND_BY: various query methods
 * - EDGE_CASES: zero stock, concurrent operations, data consistency
 */
@DataJpaTest
@DisplayName("InventoryRepository Integration Tests")
public class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Inventory testInventory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create and persist test product
        testProduct = Product.builder()
                .name("MacBook Pro")
                .description("Laptop")
                .price(1299.99)
                .stockQuantity(100)
                .isDeleted(false)
                .build();
        testProduct = productRepository.saveAndFlush(testProduct);

        // Create and persist test inventory
        testInventory = Inventory.builder()
                .product(testProduct)
                .stock(100)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        testInventory = inventoryRepository.saveAndFlush(testInventory);
    }

    // ===================================================================
    // TEST SCENARIO 1: ADD STOCK
    // ===================================================================

    @Test
    @DisplayName("Should add stock successfully and return 1")
    void testAddStock_Success() {
        // When
        int result = inventoryRepository.addStock(testProduct.getId(), 50);

        // Then
        assertEquals(1, result);
        entityManager.refresh(testInventory);
        assertEquals(150, testInventory.getStock());
    }

    @Test
    @DisplayName("Should return 0 when adding stock to non-existent product")
    void testAddStock_ProductNotFound() {
        // When
        int result = inventoryRepository.addStock(999L, 50);

        // Then
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should add stock to multiple products independently")
    void testAddStock_MultipleProducts() {
        // Given - Create second product
        Product product2 = Product.builder()
                .name("iPhone 15")
                .price(999.99)
                .stockQuantity(50)
                .isDeleted(false)
                .build();
        product2 = productRepository.saveAndFlush(product2);

        Inventory inventory2 = Inventory.builder()
                .product(product2)
                .stock(50)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        inventory2 = inventoryRepository.saveAndFlush(inventory2);

        // When
        int result1 = inventoryRepository.addStock(testProduct.getId(), 30);
        int result2 = inventoryRepository.addStock(product2.getId(), 20);

        // Then
        assertEquals(1, result1);
        assertEquals(1, result2);
        entityManager.refresh(testInventory);
        entityManager.refresh(inventory2);
        assertEquals(130, testInventory.getStock());
        assertEquals(70, inventory2.getStock());
    }

    // ===================================================================
    // TEST SCENARIO 2: REMOVE STOCK
    // ===================================================================

    @Test
    @DisplayName("Should remove stock successfully when sufficient")
    void testRemoveStock_Success() {
        // When
        int result = inventoryRepository.removeStock(testProduct.getId(), 30);

        // Then
        assertEquals(1, result);
        entityManager.refresh(testInventory);
        assertEquals(70, testInventory.getStock());
    }

    @Test
    @DisplayName("Should return 0 when removing more stock than available")
    void testRemoveStock_InsufficientStock() {
        // When
        int result = inventoryRepository.removeStock(testProduct.getId(), 150);

        // Then
        assertEquals(0, result);
        entityManager.refresh(testInventory);
        assertEquals(100, testInventory.getStock()); // Unchanged
    }

    @Test
    @DisplayName("Should handle removing exactly the current stock")
    void testRemoveStock_ExactAmount() {
        // When
        int result = inventoryRepository.removeStock(testProduct.getId(), 100);

        // Then
        assertEquals(1, result);
        entityManager.refresh(testInventory);
        assertEquals(0, testInventory.getStock());
    }

    @Test
    @DisplayName("Should return 0 when removing from non-existent product")
    void testRemoveStock_ProductNotFound() {
        // When
        int result = inventoryRepository.removeStock(999L, 30);

        // Then
        assertEquals(0, result);
    }

    // ===================================================================
    // TEST SCENARIO 3: RESERVE STOCK
    // ===================================================================

    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserve_Success() {
        // When
        int result = inventoryRepository.reserve(testProduct.getId(), 20);

        // Then
        assertEquals(1, result);
        entityManager.refresh(testInventory);
        assertEquals(80, testInventory.getStock());
        assertEquals(20, testInventory.getReserved());
    }

    @Test
    @DisplayName("Should return 0 when insufficient stock to reserve")
    void testReserve_InsufficientStock() {
        // When
        int result = inventoryRepository.reserve(testProduct.getId(), 150);

        // Then
        assertEquals(0, result);
        entityManager.refresh(testInventory);
        assertEquals(100, testInventory.getStock());
        assertEquals(0, testInventory.getReserved());
    }

    @Test
    @DisplayName("Should handle multiple reservations")
    void testReserve_MultipleReservations() {
        // When - First reservation
        int result1 = inventoryRepository.reserve(testProduct.getId(), 20);
        entityManager.refresh(testInventory);

        // When - Second reservation
        int result2 = inventoryRepository.reserve(testProduct.getId(), 30);

        // Then
        assertEquals(1, result1);
        assertEquals(1, result2);
        entityManager.refresh(testInventory);
        assertEquals(50, testInventory.getStock());
        assertEquals(50, testInventory.getReserved());
    }

    @Test
    @DisplayName("Should return 0 when reserving from non-existent product")
    void testReserve_ProductNotFound() {
        // When
        int result = inventoryRepository.reserve(999L, 20);

        // Then
        assertEquals(0, result);
    }

    // ===================================================================
    // TEST SCENARIO 4: MARK AS SOLD
    // ===================================================================

    @Test
    @DisplayName("Should mark reserved stock as sold")
    void testMarkAsSold_Success() {
        // Given - Reserve stock first
        inventoryRepository.reserve(testProduct.getId(), 20);
        entityManager.refresh(testInventory);

        // When
        int result = inventoryRepository.markAsSold(testProduct.getId(), 20);

        // Then
        assertEquals(1, result);
        entityManager.refresh(testInventory);
        assertEquals(80, testInventory.getStock());
        assertEquals(0, testInventory.getReserved());
        assertEquals(20, testInventory.getSoldCount());
    }

    @Test
    @DisplayName("Should return 0 when insufficient reserved stock")
    void testMarkAsSold_InsufficientReserved() {
        // Given - Only reserve 10
        inventoryRepository.reserve(testProduct.getId(), 10);
        entityManager.refresh(testInventory);

        // When - Try to mark 20 as sold
        int result = inventoryRepository.markAsSold(testProduct.getId(), 20);

        // Then
        assertEquals(0, result);
        entityManager.refresh(testInventory);
        assertEquals(10, testInventory.getReserved()); // Unchanged
        assertEquals(0, testInventory.getSoldCount()); // Unchanged
    }

    @Test
    @DisplayName("Should handle multiple mark as sold operations")
    void testMarkAsSold_MultipleOperations() {
        // Given - Reserve 50 units
        inventoryRepository.reserve(testProduct.getId(), 50);
        entityManager.refresh(testInventory);

        // When - Mark 20 as sold
        int result1 = inventoryRepository.markAsSold(testProduct.getId(), 20);
        entityManager.refresh(testInventory);

        // When - Mark another 20 as sold
        int result2 = inventoryRepository.markAsSold(testProduct.getId(), 20);

        // Then
        assertEquals(1, result1);
        assertEquals(1, result2);
        entityManager.refresh(testInventory);
        assertEquals(80, testInventory.getStock());
        assertEquals(10, testInventory.getReserved());
        assertEquals(40, testInventory.getSoldCount());
    }

    @Test
    @DisplayName("Should return 0 when marking as sold from non-existent product")
    void testMarkAsSold_ProductNotFound() {
        // When
        int result = inventoryRepository.markAsSold(999L, 20);

        // Then
        assertEquals(0, result);
    }

    // ===================================================================
    // TEST SCENARIO 5: RELEASE RESERVED
    // ===================================================================

    @Test
    @DisplayName("Should release reserved stock successfully")
    void testReleaseReserved_Success() {
        // Given - Reserve stock first
        inventoryRepository.reserve(testProduct.getId(), 30);
        entityManager.refresh(testInventory);

        // When
        int result = inventoryRepository.releaseReserved(testProduct.getId(), 30);

        // Then
        assertEquals(1, result);
        entityManager.refresh(testInventory);
        assertEquals(100, testInventory.getStock());
        assertEquals(0, testInventory.getReserved());
    }

    @Test
    @DisplayName("Should return 0 when insufficient reserved stock")
    void testReleaseReserved_InsufficientReserved() {
        // Given - Only reserve 10
        inventoryRepository.reserve(testProduct.getId(), 10);
        entityManager.refresh(testInventory);

        // When - Try to release 30
        int result = inventoryRepository.releaseReserved(testProduct.getId(), 30);

        // Then
        assertEquals(0, result);
        entityManager.refresh(testInventory);
        assertEquals(10, testInventory.getReserved()); // Unchanged
    }

    @Test
    @DisplayName("Should handle multiple release operations")
    void testReleaseReserved_MultipleOperations() {
        // Given - Reserve 50 units
        inventoryRepository.reserve(testProduct.getId(), 50);
        entityManager.refresh(testInventory);

        // When - Release 20
        int result1 = inventoryRepository.releaseReserved(testProduct.getId(), 20);
        entityManager.refresh(testInventory);

        // When - Release another 20
        int result2 = inventoryRepository.releaseReserved(testProduct.getId(), 20);

        // Then
        assertEquals(1, result1);
        assertEquals(1, result2);
        entityManager.refresh(testInventory);
        assertEquals(100, testInventory.getStock());
        assertEquals(10, testInventory.getReserved());
    }

    @Test
    @DisplayName("Should return 0 when releasing from non-existent product")
    void testReleaseReserved_ProductNotFound() {
        // When
        int result = inventoryRepository.releaseReserved(999L, 20);

        // Then
        assertEquals(0, result);
    }

    // ===================================================================
    // TEST SCENARIO 6: FIND BY QUERIES
    // ===================================================================

    @Test
    @DisplayName("Should find inventory by ID")
    void testFindById_Success() {
        // When
        Optional<Inventory> result = inventoryRepository.findById(testInventory.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testInventory.getId(), result.get().getId());
        assertEquals(100, result.get().getStock());
    }

    @Test
    @DisplayName("Should return empty when inventory not found by ID")
    void testFindById_NotFound() {
        // When
        Optional<Inventory> result = inventoryRepository.findById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find inventory by product ID")
    void testFindByProductId_Success() {
        // When
        Optional<Inventory> result = inventoryRepository.findByProductId(testProduct.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testProduct.getId(), result.get().getProduct().getId());
        assertEquals(100, result.get().getStock());
    }

    @Test
    @DisplayName("Should return empty when inventory not found by product ID")
    void testFindByProductId_NotFound() {
        // When
        Optional<Inventory> result = inventoryRepository.findByProductId(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should find all inventories")
    void testFindAll_Success() {
        // Given - Create another inventory
        Product product2 = Product.builder()
                .name("iPhone 15")
                .price(999.99)
                .stockQuantity(50)
                .isDeleted(false)
                .build();
        product2 = productRepository.saveAndFlush(product2);

        Inventory inventory2 = Inventory.builder()
                .product(product2)
                .stock(50)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        inventoryRepository.saveAndFlush(inventory2);

        // When
        List<Inventory> results = inventoryRepository.findAll();

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    // ===================================================================
    // TEST SCENARIO 7: EDGE CASES & DATA CONSISTENCY
    // ===================================================================

    @Test
    @DisplayName("Should handle zero stock edge case")
    void testEdgeCase_ZeroStock() {
        // Given - Remove all stock
        inventoryRepository.removeStock(testProduct.getId(), 100);
        entityManager.refresh(testInventory);

        // When - Try to reserve
        int result = inventoryRepository.reserve(testProduct.getId(), 1);

        // Then
        assertEquals(0, result);
        entityManager.refresh(testInventory);
        assertEquals(0, testInventory.getStock());
        assertEquals(0, testInventory.getReserved());
    }

    @Test
    @DisplayName("Should handle large quantity operations")
    void testEdgeCase_LargeQuantities() {
        // Given
        testInventory.setStock(10000);
        inventoryRepository.save(testInventory);
        entityManager.refresh(testInventory);

        // When
        int addResult = inventoryRepository.addStock(testProduct.getId(), 5000);
        entityManager.refresh(testInventory);
        int reserveResult = inventoryRepository.reserve(testProduct.getId(), 5000);

        // Then
        assertEquals(1, addResult);
        assertEquals(1, reserveResult);
        entityManager.refresh(testInventory);
        assertEquals(10000, testInventory.getStock());
        assertEquals(5000, testInventory.getReserved());
    }

    @Test
    @DisplayName("Should maintain data consistency across operation sequence")
    void testEdgeCase_DataConsistency() {
        // When - Perform complex operation sequence
        inventoryRepository.addStock(testProduct.getId(), 50);     // 150
        entityManager.refresh(testInventory);
        inventoryRepository.reserve(testProduct.getId(), 40);      // 110 avail, 40 reserved
        entityManager.refresh(testInventory);
        inventoryRepository.markAsSold(testProduct.getId(), 40);   // 110 avail, 0 reserved, 40 sold
        entityManager.refresh(testInventory);
        inventoryRepository.addStock(testProduct.getId(), 25);     // 135 avail, 0 reserved, 40 sold
        entityManager.refresh(testInventory);

        // Then
        assertEquals(135, testInventory.getStock());
        assertEquals(0, testInventory.getReserved());
        assertEquals(40, testInventory.getSoldCount());
    }

    @Test
    @DisplayName("Should handle concurrent-like operations atomically")
    void testEdgeCase_AtomicOperations() {
        // Simulate concurrent-like operations using transaction isolation

        // Operation 1: Reserve
        inventoryRepository.reserve(testProduct.getId(), 25);
        entityManager.refresh(testInventory);

        // Operation 2: Add stock (can happen while reserved)
        inventoryRepository.addStock(testProduct.getId(), 50);
        entityManager.refresh(testInventory);

        // Operation 3: Mark sold (only reserved amount)
        inventoryRepository.markAsSold(testProduct.getId(), 25);
        entityManager.refresh(testInventory);

        // Operation 4: Release remaining (should be 0)
        inventoryRepository.releaseReserved(testProduct.getId(), 0);

        // Then - Verify final atomic state
        assertEquals(125, testInventory.getStock()); // 100 + 50 - 25
        assertEquals(0, testInventory.getReserved());
        assertEquals(25, testInventory.getSoldCount());
    }

    @Test
    @DisplayName("Should maintain separate inventory states for different products")
    void testEdgeCase_ProductIsolation() {
        // Given - Create second product with inventory
        Product product2 = Product.builder()
                .name("iPad Pro")
                .price(1099.99)
                .stockQuantity(30)
                .isDeleted(false)
                .build();
        product2 = productRepository.saveAndFlush(product2);

        Inventory inventory2 = Inventory.builder()
                .product(product2)
                .stock(30)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        inventory2 = inventoryRepository.saveAndFlush(inventory2);

        // When - Perform operations on both
        inventoryRepository.reserve(testProduct.getId(), 20);
        inventoryRepository.addStock(product2.getId(), 20);

        // Then - Verify isolation
        entityManager.refresh(testInventory);
        entityManager.refresh(inventory2);
        assertEquals(80, testInventory.getStock());
        assertEquals(20, testInventory.getReserved());
        assertEquals(50, inventory2.getStock());
        assertEquals(0, inventory2.getReserved());
    }
}
