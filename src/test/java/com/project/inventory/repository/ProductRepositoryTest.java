package com.project.inventory.repository;

import com.project.inventory.configuration.TestCacheConfig;
import com.project.inventory.entity.Product;
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
@DisplayName("ProductRepository Integration Tests")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EntityManager entityManager;

    private Product testProduct;
    private Product deletedProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .name("iPhone 15 Pro Max")
                .description("Latest Apple flagship")
                .price(1999.99)
                .stockQuantity(100)
                .isDeleted(false)
                .build();
        testProduct = productRepository.save(testProduct);

        deletedProduct = Product.builder()
                .name("Old Product")
                .price(99.99)
                .stockQuantity(50)
                .isDeleted(true)
                .build();
        deletedProduct = productRepository.save(deletedProduct);
    }

    @Test
    @DisplayName("Should create and find active product")
    void testCreateAndFindProduct() {
        Product product = Product.builder()
                .name("Samsung Galaxy S24")
                .price(899.99)
                .stockQuantity(75)
                .isDeleted(false)
                .build();
        
        Product saved = productRepository.save(product);
        
        assertNotNull(saved.getId());
        assertEquals("Samsung Galaxy S24", saved.getName());
        assertEquals(899.99, saved.getPrice());
    }

    @Test
    @DisplayName("Should find active product by ID")
    void testFindActiveById() {
        Optional<Product> result = productRepository.findActiveById(testProduct.getId());
        
        assertTrue(result.isPresent());
        assertEquals(testProduct.getId(), result.get().getId());
        assertEquals("iPhone 15 Pro Max", result.get().getName());
    }

    @Test
    @DisplayName("Should not find deleted product by ID")
    void testFindActiveById_NotFound() {
        Optional<Product> result = productRepository.findActiveById(deletedProduct.getId());
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find non-existent product returns empty")
    void testFindActiveById_NonExistent() {
        Optional<Product> result = productRepository.findActiveById(999L);
        
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should find all active products with pagination")
    void testFindAllActive_Paginated() {
        Page<Product> result = productRepository.findAllActive(PageRequest.of(0, 10));
        
        assertNotNull(result);
        assertTrue(result.getContent().size() > 0);
        assertTrue(result.getContent().stream()
                .allMatch(p -> !p.getIsDeleted()));
    }

    @Test
    @DisplayName("Should find all active products (no pagination)")
    void testFindAllActive_List() {
        List<Product> result = productRepository.findAllActive();
        
        assertNotNull(result);
        assertTrue(result.size() > 0);
        assertTrue(result.stream()
                .allMatch(p -> !p.getIsDeleted()));
    }

    @Test
    @DisplayName("Should exclude deleted products from active list")
    void testFindAllActive_ExcludesDeleted() {
        List<Product> result = productRepository.findAllActive();
        
        assertFalse(result.stream().anyMatch(p -> p.getId().equals(deletedProduct.getId())));
        assertTrue(result.stream().anyMatch(p -> p.getId().equals(testProduct.getId())));
    }

    @Test
    @DisplayName("Should soft delete product successfully")
    void testSoftDeleteById() {
        int result = productRepository.softDeleteById(testProduct.getId());
        entityManager.flush();
        entityManager.clear();
        
        assertEquals(1, result);
        
        Optional<Product> found = productRepository.findActiveById(testProduct.getId());
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should not soft delete already deleted product")
    void testSoftDeleteById_AlreadyDeleted() {
        int result = productRepository.softDeleteById(deletedProduct.getId());
        
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should not soft delete non-existent product")
    void testSoftDeleteById_NonExistent() {
        int result = productRepository.softDeleteById(999L);
        
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should deduct stock successfully")
    void testDeductStock() {
        int result = productRepository.deductStock(testProduct.getId(), 30);
        entityManager.flush();
        entityManager.clear();
        
        assertEquals(1, result);
        
        Product updated = productRepository.findActiveById(testProduct.getId()).orElseThrow();
        assertEquals(70, updated.getStockQuantity());
    }

    @Test
    @DisplayName("Should fail to deduct more stock than available")
    void testDeductStock_InsufficientStock() {
        int result = productRepository.deductStock(testProduct.getId(), 200);
        
        assertEquals(0, result);
        
        Product unchanged = productRepository.findActiveById(testProduct.getId()).orElseThrow();
        assertEquals(100, unchanged.getStockQuantity());
    }

    @Test
    @DisplayName("Should not deduct stock from deleted product")
    void testDeductStock_DeletedProduct() {
        int result = productRepository.deductStock(deletedProduct.getId(), 10);
        
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should not deduct stock from non-existent product")
    void testDeductStock_NonExistent() {
        int result = productRepository.deductStock(999L, 10);
        
        assertEquals(0, result);
    }

    @Test
    @DisplayName("Should handle multiple concurrent deductions")
    void testDeductStock_Concurrent() {
        int result1 = productRepository.deductStock(testProduct.getId(), 20);
        int result2 = productRepository.deductStock(testProduct.getId(), 30);
        entityManager.flush();
        entityManager.clear();
        
        assertEquals(1, result1);
        assertEquals(1, result2);
        
        Product updated = productRepository.findActiveById(testProduct.getId()).orElseThrow();
        assertEquals(50, updated.getStockQuantity());
    }

    @Test
    @DisplayName("Should deduct zero quantity")
    void testDeductStock_Zero() {
        int result = productRepository.deductStock(testProduct.getId(), 0);
        entityManager.flush();
        entityManager.clear();
        
        assertEquals(1, result);
        
        Product unchanged = productRepository.findActiveById(testProduct.getId()).orElseThrow();
        assertEquals(100, unchanged.getStockQuantity());
    }

    @Test
    @DisplayName("Should update product details")
    void testUpdateProduct() {
        testProduct.setName("iPhone 16 Pro Max");
        testProduct.setPrice(2099.99);
        testProduct.setDescription("New generation");
        
        Product updated = productRepository.save(testProduct);
        entityManager.flush();
        entityManager.clear();
        
        Product found = productRepository.findActiveById(updated.getId()).orElseThrow();
        assertEquals("iPhone 16 Pro Max", found.getName());
        assertEquals(2099.99, found.getPrice());
        assertEquals("New generation", found.getDescription());
    }

    @Test
    @DisplayName("Should retrieve product with all fields populated")
    void testProductAllFields() {
        Product product = Product.builder()
                .name("Premium Product")
                .description("Premium description")
                .price(1500.00)
                .stockQuantity(50)
                .image("image.jpg")
                .isDeleted(false)
                .build();
        
        Product saved = productRepository.save(product);
        Product found = productRepository.findActiveById(saved.getId()).orElseThrow();
        
        assertEquals("Premium Product", found.getName());
        assertEquals("Premium description", found.getDescription());
        assertEquals(1500.00, found.getPrice());
        assertEquals(50, found.getStockQuantity());
        assertEquals("image.jpg", found.getImage());
        assertFalse(found.getIsDeleted());
    }

    @Test
    @DisplayName("Should handle version field for optimistic locking")
    void testVersionField() {
        Long initialVersion = testProduct.getVersion();
        
        testProduct.setName("Updated Name");
        productRepository.save(testProduct);
        entityManager.flush();
        
        Product found = productRepository.findActiveById(testProduct.getId()).orElseThrow();
        assertNotNull(found.getVersion());
    }
}
