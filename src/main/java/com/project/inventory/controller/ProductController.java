package com.project.inventory.controller;

import com.project.inventory.dto.product.ProductRequestDTO;
import com.project.inventory.dto.product.ProductResponseDTO;
import com.project.inventory.dto.response.ApiResponse;
import com.project.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ProductController - REST API for product management
 * 
 * Endpoints:
 * - GET /api/products (list, cached)
 * - GET /api/products/{id} (detail, not cached, always live)
 * - POST /api/products (admin only, cache eviction)
 * - PUT /api/products/{id} (admin only, cache eviction)
 * - DELETE /api/products/{id} (admin only, soft delete, cache eviction)
 */
@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    @Operation(summary = "Create product", description = "Create new product (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(@Valid @RequestBody ProductRequestDTO request) {
        log.info("Creating new product: {}", request.getName());
        
        ProductResponseDTO newProduct = productService.createProduct(request);

        ApiResponse<ProductResponseDTO> response = ApiResponse.<ProductResponseDTO>builder()
                .code(HttpStatus.CREATED.value())
                .message("Product created successfully")
                .data(newProduct)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "List all products with optional filters", description = "Get paginated list of products with category, search, and price filters")
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        log.debug("Fetching products - page: {}, size: {}, category: {}, search: {}, price: {}-{}", 
                page, size, category, search, minPrice, maxPrice);
        
        // Create sort and pageable objects
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Determine if filters are provided
        boolean hasFilters = category != null || search != null || minPrice != null || maxPrice != null;
        
        // Fetch data from service
        Page<ProductResponseDTO> products;
        if (hasFilters) {
            products = productService.getProductsByFilters(category, search, minPrice, maxPrice, pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        // Wrap in API response
        ApiResponse<Page<ProductResponseDTO>> response = ApiResponse.<Page<ProductResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .message("Products retrieved successfully")
                .data(products)
                .build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(response);
    }

    @GetMapping("/categories/list")
    @Transactional(readOnly = true)
    @Operation(summary = "Get all product categories", description = "Get distinct list of categories from active products")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        log.debug("Fetching product categories");
        
        List<String> categories = productService.getCategories();

        ApiResponse<List<String>> response = ApiResponse.<List<String>>builder()
                .code(HttpStatus.OK.value())
                .message("Categories retrieved successfully")
                .data(categories)
                .build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(response);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "Get product by ID", description = "Get detailed product information (not cached, always live)")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id) {
        log.debug("Fetching product by ID: {}", id);
        
        ProductResponseDTO product = productService.getProductById(id);

        ApiResponse<ProductResponseDTO> response = ApiResponse.<ProductResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("Product retrieved successfully")
                .data(product)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    @Operation(summary = "Update product", description = "Update product information (Admin only)")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductRequestDTO request
    ) {
        log.info("Updating product with ID: {}", id);
        
        ProductResponseDTO updatedProduct = productService.updateProduct(id, request);

        ApiResponse<ProductResponseDTO> response = ApiResponse.<ProductResponseDTO>builder()
                .code(HttpStatus.OK.value())
                .message("Product updated successfully")
                .data(updatedProduct)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    @Operation(summary = "Delete product", description = "Soft delete product (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("Soft deleting product with ID: {}", id);
        
        productService.deleteProduct(id);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Product deleted successfully")
                .build();

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
