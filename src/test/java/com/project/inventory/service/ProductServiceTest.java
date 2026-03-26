package com.project.inventory.service;

import com.project.inventory.dto.product.ProductRequestDTO;
import com.project.inventory.dto.product.ProductResponseDTO;
import com.project.inventory.entity.Product;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    // ===================================================================
    // TEST SCENARIO 1: GET BY ID
    // ===================================================================

    @Test
    @DisplayName("Should return ProductResponseDTO when product exists")
    void getProductById_WhenProductExists_ShouldReturnProductResponseDTO() {
        // Given
        Long productId = 1L;
        Product mockProduct = Product.builder()
                .name("Mechanical Keyboard")
                .price(2000000.0)
                .stockQuantity(10)
                .build();
        mockProduct.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // When
        ProductResponseDTO result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Mechanical Keyboard", result.getName());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void getProductById_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        Long fakeId = 999L;
        when(productRepository.findById(fakeId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(fakeId)
        );
        assertTrue(exception.getMessage().contains("Not found"));
        verify(productRepository, times(1)).findById(fakeId);
    }

    // ===================================================================
    // TEST SCENARIO 2: CREATE
    // ===================================================================

    @Test
    @DisplayName("Should return ProductResponseDTO when creating product successfully")
    void createProduct_ShouldReturnProductResponseDTO() {
        // Given
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setName("RGB Gaming Mouse");
        requestDTO.setPrice(500000.0);
        requestDTO.setStockQuantity(20);

        Product savedProduct = Product.builder()
                .name(requestDTO.getName())
                .price(requestDTO.getPrice())
                .stockQuantity(requestDTO.getStockQuantity())
                .build();
        savedProduct.setId(100L);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductResponseDTO result = productService.createProduct(requestDTO);

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(requestDTO.getName(), result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ===================================================================
    // TEST SCENARIO 3: UPDATE
    // ===================================================================

    @Test
    @DisplayName("Should update product and return DTO when product exists")
    void updateProduct_WhenProductExists_ShouldReturnUpdatedProductDTO() {
        // Given
        Long productId = 1L;
        ProductRequestDTO updateRequest = new ProductRequestDTO();
        updateRequest.setName("Mechanical Keyboard v2");
        updateRequest.setPrice(2500000.0);
        updateRequest.setStockQuantity(15);

        Product existingProduct = Product.builder()
                .name("Mechanical Keyboard v1")
                .price(2000000.0)
                .stockQuantity(10)
                .build();
        existingProduct.setId(productId);

        Product updatedProduct = Product.builder()
                .name(updateRequest.getName())
                .price(updateRequest.getPrice())
                .stockQuantity(updateRequest.getStockQuantity())
                .build();
        updatedProduct.setId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        ProductResponseDTO result = productService.updateProduct(productId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals(updateRequest.getName(), result.getName());
        assertEquals(updateRequest.getPrice(), result.getPrice());

        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    void updateProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        Long productId = 99L;
        ProductRequestDTO updateRequest = new ProductRequestDTO();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(productId, updateRequest));
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    // ===================================================================
    // KỊCH BẢN 4: DELETE
    // ===================================================================

    @Test
    @DisplayName("Should delete product successfully when product exists")
    void deleteProduct_WhenProductExists_ShouldExecuteWithoutErrors() {
        // Given
        Long productId = 1L;
        when(productRepository.existsById(productId)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> productService.deleteProduct(productId));

        // Then
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    void deleteProduct_WhenProductDoesNotExist_ShouldThrowException() {
        // Given
        Long productId = 99L;
        when(productRepository.existsById(productId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(productId));

        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, never()).deleteById(anyLong());
    }
}