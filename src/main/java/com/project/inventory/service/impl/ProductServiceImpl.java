package com.project.inventory.service.impl;

import com.project.inventory.dto.product.ProductRequestDTO;
import com.project.inventory.dto.product.ProductResponseDTO;
import com.project.inventory.entity.Product;
import com.project.inventory.entity.ProductAction;
import com.project.inventory.entity.ProductHistory;
import com.project.inventory.exception.ResourceNotFoundException;
import com.project.inventory.repository.ProductHistoryRepository;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProductServiceImpl - Product business logic with soft delete and audit logging
 * 
 * Features:
 * - Soft delete (set is_deleted = true instead of removing)
 * - Audit log (track CREATE, UPDATE, DELETE operations)
 * - Transaction safety (@Transactional on write ops)
 * - Security integration (track who made changes)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductHistoryRepository productHistoryRepository;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        log.info("Creating new product: {}", request.getName());
        
        // Create product entity
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .image(request.getImage())
                .isDeleted(false)
                .build();

        // Save product
        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());

        // Log to audit trail
        logProductHistory(savedProduct, ProductAction.CREATE);

        return mapToResponseDTO(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all active products with pagination");
        
        // Query only non-deleted products
        Page<Product> productPage = productRepository.findAllActive(pageable);

        return productPage.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'filtered-' + (#category ?: 'all') + '-' + (#search ?: 'none') + '-' + #minPrice + '-' + #maxPrice + '-' + #pageable.pageNumber")
    public Page<ProductResponseDTO> getProductsByFilters(String category, String search, Double minPrice, Double maxPrice, Pageable pageable) {
        log.debug("Fetching products with filters - category: {}, search: {}, price: {}-{}", category, search, minPrice, maxPrice);
        
        // Query with filters
        Page<Product> productPage = productRepository.findByFilters(category, search, minPrice, maxPrice, pageable);

        return productPage.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponseDTO getProductById(Long id) {
        log.debug("Fetching product by ID: {}", id);
        
        // Query only non-deleted products
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found with ID: {}", id);
                    return new ResourceNotFoundException("Product not found with ID: " + id);
                });

        return mapToResponseDTO(product);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
        log.info("Updating product with ID: {}", id);
        
        // Find active product
        Product existingProduct = productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Update fields
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setImage(request.getImage());

        // Save (JPA detects changes and performs UPDATE)
        Product updatedProduct = productRepository.save(existingProduct);
        log.info("Product updated successfully with ID: {}", id);

        // Log to audit trail
        logProductHistory(updatedProduct, ProductAction.UPDATE);

        return mapToResponseDTO(updatedProduct);
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        log.info("Soft deleting product with ID: {}", id);
        
        // Check if product exists and is not already deleted
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found or already deleted with ID: {}", id);
                    return new ResourceNotFoundException("Product not found or already deleted with ID: " + id);
                });

        // Soft delete: set is_deleted = true
        int result = productRepository.softDeleteById(id);
        
        if (result == 0) {
            log.error("Failed to soft delete product with ID: {}", id);
            throw new ResourceNotFoundException("Failed to delete product with ID: " + id);
        }

        // Refresh product to get latest state for logging
        product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        log.info("Product soft deleted successfully with ID: {}", id);

        // Log to audit trail
        logProductHistory(product, ProductAction.DELETE);
    }

    /**
     * Helper method to log product changes to audit trail
     * Extracts current username from SecurityContext
     */
    private void logProductHistory(Product product, ProductAction action) {
        // Get current username (null-safe)
        String username = "SYSTEM";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        // Create history record
        ProductHistory history = ProductHistory.builder()
                .productId(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .action(action)
                .changedBy(username)
                .build();

        // Save to audit log
        productHistoryRepository.save(history);
        log.debug("Logged {} action for product ID: {} by user: {}", 
                action, product.getId(), username);
    }

    /**
     * Helper method to map Product entity to ResponseDTO
     */
    private ProductResponseDTO mapToResponseDTO(Product product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImage())
                .category(product.getCategory())
                .isDeleted(product.getIsDeleted())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
