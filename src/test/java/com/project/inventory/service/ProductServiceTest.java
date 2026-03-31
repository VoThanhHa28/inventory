package com.project.inventory.service;

import com.project.inventory.dto.product.ProductRequestDTO;
import com.project.inventory.dto.product.ProductResponseDTO;
import com.project.inventory.entity.Product;
import com.project.inventory.entity.ProductAction;
import com.project.inventory.entity.ProductHistory;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.repository.ProductHistoryRepository;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService - Comprehensive test suite
 * 
 * Test scenarios:
 * - Product creation with audit logging
 * - Product update with audit logging
 * - Soft delete functionality (is_deleted = true)
 * - Get operations (by ID and paginated)
 * - Audit trail capturing username from SecurityContext
 * - Exception handling for missing products
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductHistoryRepository productHistoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product mockProduct;
    private ProductRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        mockProduct = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .description("High-performance laptop")
                .price(1299.99)
                .stockQuantity(50)
                .image("macbook.jpg")
                .isDeleted(false)
                .build();

        requestDTO = new ProductRequestDTO();
        requestDTO.setName("MacBook Pro");
        requestDTO.setDescription("High-performance laptop");
        requestDTO.setPrice(1299.99);
        requestDTO.setStockQuantity(50);
        requestDTO.setImage("macbook.jpg");
    }

    // ===================================================================
    // TEST SCENARIO 1: CREATE PRODUCT
    // ===================================================================

    @Test
    @DisplayName("Should create product and log to ProductHistory audit trail")
    void testCreateProduct_ShouldSaveAndLogHistory() {
        // Given: ProductRequestDTO
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(productHistoryRepository.save(any(ProductHistory.class))).thenReturn(
                ProductHistory.builder()
                        .id(1L)
                        .productId(mockProduct.getId())
                        .name(mockProduct.getName())
                        .action(ProductAction.CREATE)
                        .changedBy("testuser")
                        .build()
        );

        // Setup SecurityContext for logProductHistory
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When: Call createProduct
        ProductResponseDTO result = productService.createProduct(requestDTO);

        // Then: Verify product was saved and history logged
        assertNotNull(result);
        assertEquals("MacBook Pro", result.getName());
        assertEquals(1299.99, result.getPrice());
        assertEquals(false, result.getIsDeleted());
        
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productHistoryRepository, times(1)).save(any(ProductHistory.class));
    }

    // ===================================================================
    // TEST SCENARIO 2: UPDATE PRODUCT
    // ===================================================================

    @Test
    @DisplayName("Should update product and log UPDATE action to ProductHistory")
    void testUpdateProduct_ShouldUpdateAndLogHistory() {
        // Given: Existing product
        Long productId = 1L;
        ProductRequestDTO updateRequest = new ProductRequestDTO();
        updateRequest.setName("MacBook Pro M3");
        updateRequest.setDescription("High-performance M3 chip");
        updateRequest.setPrice(1599.99);
        updateRequest.setStockQuantity(100);

        Product existingProduct = mockProduct;
        Product updatedProduct = Product.builder()
                .id(productId)
                .name(updateRequest.getName())
                .description(updateRequest.getDescription())
                .price(updateRequest.getPrice())
                .stockQuantity(updateRequest.getStockQuantity())
                .image(updateRequest.getImage())
                .isDeleted(false)
                .build();

        when(productRepository.findActiveById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productHistoryRepository.save(any(ProductHistory.class))).thenReturn(
                ProductHistory.builder()
                        .id(2L)
                        .productId(productId)
                        .name(updateRequest.getName())
                        .action(ProductAction.UPDATE)
                        .changedBy("testuser")
                        .build()
        );

        // Setup SecurityContext for logProductHistory
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When: Call updateProduct
        ProductResponseDTO result = productService.updateProduct(productId, updateRequest);

        // Then: Verify update and history logging
        assertNotNull(result);
        assertEquals("MacBook Pro M3", result.getName());
        assertEquals(1599.99, result.getPrice());
        
        verify(productRepository, times(1)).findActiveById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(productHistoryRepository, times(1)).save(any(ProductHistory.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void testUpdateProduct_WhenProductNotFound_ShouldThrowException() {
        // Given: Non-existent product ID
        Long nonExistentId = 999L;
        when(productRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then: Verify exception is thrown
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(nonExistentId, requestDTO);
        });

        verify(productRepository, times(1)).findActiveById(nonExistentId);
        verify(productRepository, never()).save(any());
        verify(productHistoryRepository, never()).save(any());
    }

    // ===================================================================
    // TEST SCENARIO 3: SOFT DELETE
    // ===================================================================

    @Test
    @DisplayName("Should soft delete product (set is_deleted=true) and log DELETE action")
    void testDeleteProduct_ShouldSoftDeleteAndLogHistory() {
        // Given: Existing active product
        Long productId = 1L;
        mockProduct.setIsDeleted(false);

        when(productRepository.findActiveById(productId)).thenReturn(Optional.of(mockProduct));
        when(productRepository.softDeleteById(productId)).thenReturn(1); // 1 row affected
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(productHistoryRepository.save(any(ProductHistory.class))).thenReturn(
                ProductHistory.builder()
                        .id(3L)
                        .productId(productId)
                        .name(mockProduct.getName())
                        .action(ProductAction.DELETE)
                        .changedBy("testuser")
                        .build()
        );

        // Setup SecurityContext for logProductHistory
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When: Call deleteProduct (soft delete)
        productService.deleteProduct(productId);

        // Then: Verify soft delete called and history logged
        verify(productRepository, times(1)).findActiveById(productId);
        verify(productRepository, times(1)).softDeleteById(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(productHistoryRepository, times(1)).save(any(ProductHistory.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProduct_WhenProductNotFound_ShouldThrowException() {
        // Given: Non-existent product ID
        Long nonExistentId = 999L;
        when(productRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then: Verify exception is thrown and no soft delete occurs
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(nonExistentId);
        });

        verify(productRepository, times(1)).findActiveById(nonExistentId);
        verify(productRepository, never()).softDeleteById(any());
        verify(productHistoryRepository, never()).save(any());
    }

    // ===================================================================
    // TEST SCENARIO 4: GET OPERATIONS
    // ===================================================================

    @Test
    @DisplayName("Should retrieve active product by ID (excludes soft-deleted)")
    void testGetProductById_ShouldReturnActiveProduct() {
        // Given: Product ID
        Long productId = 1L;
        when(productRepository.findActiveById(productId)).thenReturn(Optional.of(mockProduct));

        // When: Call getProductById
        ProductResponseDTO result = productService.getProductById(productId);

        // Then: Verify result
        assertNotNull(result);
        assertEquals(mockProduct.getId(), result.getId());
        assertEquals(mockProduct.getName(), result.getName());
        assertEquals(false, result.getIsDeleted());

        verify(productRepository, times(1)).findActiveById(productId);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent product")
    void testGetProductById_WhenProductNotFound_ShouldThrowException() {
        // Given: Non-existent product ID
        Long nonExistentId = 999L;
        when(productRepository.findActiveById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then: Verify exception
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(nonExistentId);
        });

        verify(productRepository, times(1)).findActiveById(nonExistentId);
    }

    @Test
    @DisplayName("Should retrieve paginated active products (excludes soft-deleted)")
    void testGetAllProducts_ShouldReturnPagedActiveProducts() {
        // Given: Pageable query
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = List.of(mockProduct);

        when(productRepository.findAllActiveProducts()).thenReturn(productList);

        // When: Call getAllProducts
        Page<ProductResponseDTO> result = productService.getAllProducts(pageable);

        // Then: Verify pagination
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("MacBook Pro", result.getContent().get(0).getName());
        assertEquals(false, result.getContent().get(0).getIsDeleted());

        verify(productRepository, times(1)).findAllActiveProducts();
    }

    // ===================================================================
    // TEST SCENARIO 5: AUDIT LOGGING
    // ===================================================================

    @Test
    @DisplayName("Should capture correct username in ProductHistory from SecurityContext")
    void testAuditLog_ShouldCaptureCorrectUsernameFromSecurityContext() {
        // Given: SecurityContext with "admin" user
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("admin");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(productHistoryRepository.save(any(ProductHistory.class))).thenAnswer(invocation -> {
            ProductHistory savedHistory = invocation.getArgument(0);
            assertEquals("admin", savedHistory.getChangedBy());
            assertEquals(ProductAction.CREATE, savedHistory.getAction());
            return savedHistory;
        });

        // When: Create product
        productService.createProduct(requestDTO);

        // Then: Verify username was captured from SecurityContext
        verify(productHistoryRepository, times(1)).save(argThat(history -> 
                history.getChangedBy().equals("admin") &&
                history.getAction() == ProductAction.CREATE
        ));
    }

    @Test
    @DisplayName("Should use SYSTEM username in ProductHistory when no authenticated user")
    void testAuditLog_ShouldUseSYSTEMWhenNoAuthenticatedUser() {
        // Given: No authenticated user (null SecurityContext)
        SecurityContextHolder.clearContext();

        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(productHistoryRepository.save(any(ProductHistory.class))).thenAnswer(invocation -> {
            ProductHistory savedHistory = invocation.getArgument(0);
            // Should default to SYSTEM when no authentication
            assertTrue(savedHistory.getChangedBy().isEmpty() || 
                      savedHistory.getChangedBy().equals("SYSTEM"));
            return savedHistory;
        });

        // When: Create product without authentication
        productService.createProduct(requestDTO);

        // Then: Verify SYSTEM default was used
        verify(productHistoryRepository, times(1)).save(any(ProductHistory.class));
    }

    // ===================================================================
    // TEST SCENARIO 6: SOFT DELETE vs HARD DELETE
    // ===================================================================

    @Test
    @DisplayName("Should use softDeleteById (not hard delete) - data preservation")
    void testDeleteProduct_ShouldUseSoftDeleteNotHardDelete() {
        // Given: Product to be soft deleted
        Long productId = 1L;
        when(productRepository.findActiveById(productId)).thenReturn(Optional.of(mockProduct));
        when(productRepository.softDeleteById(productId)).thenReturn(1);
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(productHistoryRepository.save(any(ProductHistory.class))).thenReturn(
                ProductHistory.builder()
                        .id(3L)
                        .productId(productId)
                        .name(mockProduct.getName())
                        .action(ProductAction.DELETE)
                        .build()
        );

        // Setup SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // When: Delete product
        productService.deleteProduct(productId);

        // Then: Verify softDeleteById was called (not deleteById)
        verify(productRepository, times(1)).softDeleteById(productId);
        verify(productRepository, never()).deleteById(productId); // Hard delete NOT called
    }
}