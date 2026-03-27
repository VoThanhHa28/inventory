package com.project.inventory.repository;

import com.project.inventory.entity.Inventory;
import com.project.inventory.entity.InventoryStatus;
import com.project.inventory.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("InventoryRepository Integration Tests")
public class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    private Product testProduct;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .name("Test Product")
                .price(99.99)
                .stockQuantity(100)
                .isDeleted(false)
                .build();
        testProduct = productRepository.save(testProduct);

        testInventory = Inventory.builder()
                .product(testProduct)
                .stock(100)
                .reserved(0)
                .soldCount(0)
                .status(InventoryStatus.AVAILABLE)
                .build();
        testInventory = inventoryRepository.save(testInventory);
    }

    @Test
    @DisplayName("Should add stock successfully")
    void testAddStock_Success() {
        int result = inventoryRepository.addStock(testProduct.getId(), 50);
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, result);

        Inventory updated = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow();
        assertEquals(150, updated.getStock());
    }

    @Test
    @DisplayName("Should add stock to non-existent product")
    void testAddStock_NonExistent() {
        int result = inventoryRepository.addStock(999L, 50);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should remove stock successfully")
    void testRemoveStock_Success() {
        int result = inventoryRepository.removeStock(testProduct.getId(), 30);
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, result);

        Inventory updated = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow();
        assertEquals(70, updated.getStock());
    }

    @Test
    @DisplayName("Should fail to remove more stock than available")
    void testRemoveStock_InsufficientStock() {
        int result = inventoryRepository.removeStock(testProduct.getId(), 200);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserve_Success() {
        int result = inventoryRepository.reserve(testProduct.getId(), 40);
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, result);

        Inventory updated = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow();
        assertEquals(60, updated.getStock());
        assertEquals(40, updated.getReserved());
    }

    @Test
    @DisplayName("Should fail to reserve more than available")
    void testReserve_InsufficientStock() {
        int result = inventoryRepository.reserve(testProduct.getId(), 200);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should mark reserved stock as sold")
    void testMarkAsSold_Success() {
        inventoryRepository.reserve(testProduct.getId(), 25);
        entityManager.flush();
        entityManager.clear();
        
        int result = inventoryRepository.markAsSold(testProduct.getId(), 25);
        entityManager.flush();
        entityManager.clear();
        assertEquals(1, result);

        Inventory updated = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow();
        assertEquals(0, updated.getReserved());
        assertEquals(25, updated.getSoldCount());
    }

    @Test
    @DisplayName("Should fail to mark as sold when insufficient reserved")
    void testMarkAsSold_InsufficientReserved() {
        int result = inventoryRepository.markAsSold(testProduct.getId(), 50);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should release reserved stock successfully")
    void testReleaseReserved_Success() {
        inventoryRepository.reserve(testProduct.getId(), 20);
        entityManager.flush();
        
        int result = inventoryRepository.releaseReserved(testProduct.getId(), 20);
        entityManager.flush();
        assertEquals(1, result);

        Inventory updated = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow();
        assertEquals(100, updated.getStock());
        assertEquals(0, updated.getReserved());
    }

    @Test
    @DisplayName("Should fail to release more than reserved")
    void testReleaseReserved_InsufficientReserved() {
        inventoryRepository.reserve(testProduct.getId(), 10);
        entityManager.flush();
        
        int result = inventoryRepository.releaseReserved(testProduct.getId(), 50);
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should find inventory by product ID")
    void testFindByProductId_Success() {
        Optional<Inventory> result = inventoryRepository.findByProductId(testProduct.getId());
        
        assertTrue(result.isPresent());
        assertEquals(testInventory.getId(), result.get().getId());
        assertEquals(testProduct.getId(), result.get().getProduct().getId());
    }

    @Test
    @DisplayName("Should return empty for non-existent product ID")
    void testFindByProductId_NotFound() {
        Optional<Inventory> result = inventoryRepository.findByProductId(999L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should retrieve all inventory records")
    void testFindAll_Success() {
        var result = inventoryRepository.findAll();
        
        assertNotNull(result);
        assertTrue(result.size() > 0);
    }

    @Test
    @DisplayName("Should retrieve inventory by ID")
    void testFindById_Success() {
        Optional<Inventory> result = inventoryRepository.findById(testInventory.getId());
        
        assertTrue(result.isPresent());
        assertEquals(testInventory.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Should return empty for non-existent inventory ID")
    void testFindById_NotFound() {
        Optional<Inventory> result = inventoryRepository.findById(999L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle concurrent stock additions")
    void testConcurrentOperations() {
        int result1 = inventoryRepository.addStock(testProduct.getId(), 50);
        int result2 = inventoryRepository.addStock(testProduct.getId(), 30);
        entityManager.flush();
        entityManager.clear();
        
        assertEquals(1, result1);
        assertEquals(1, result2);

        Inventory updated = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow();
        assertEquals(180, updated.getStock());
    }

    @Test
    @DisplayName("Should handle inventory status updates")
    void testInventoryStatusUpdate() {
        testInventory.setStatus(InventoryStatus.RESERVED);
        inventoryRepository.save(testInventory);
        entityManager.flush();

        Inventory updated = inventoryRepository.findById(testInventory.getId()).orElseThrow();
        assertEquals(InventoryStatus.RESERVED, updated.getStatus());
    }

    @Test
    @DisplayName("Should calculate available stock correctly")
    void testCalculateAvailableStock() {
        inventoryRepository.reserve(testProduct.getId(), 30);
        entityManager.flush();
        entityManager.clear();

        Inventory updated = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow();
        // available = stock - reserved - soldCount = 70 - 30 - 0 = 40
        assertEquals(70, updated.getStock());
        assertEquals(30, updated.getReserved());
        assertEquals(0, updated.getSoldCount());
    }

    @Test
    @DisplayName("Should handle zero quantity operations")
    void testZeroQuantityOperations() {
        int result = inventoryRepository.addStock(testProduct.getId(), 0);
        assertEquals(1, result);

        Inventory updated = inventoryRepository.findByProductId(testProduct.getId()).orElseThrow();
        assertEquals(100, updated.getStock());
    }
}
