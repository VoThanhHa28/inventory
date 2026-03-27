package com.project.inventory.service;

import com.project.inventory.dto.product.ProductRequestDTO;
import com.project.inventory.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request);

    Page<ProductResponseDTO> getAllProducts(Pageable pageable);

    Page<ProductResponseDTO> getProductsByFilters(String category, String search, Double minPrice, Double maxPrice, Pageable pageable);

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO request);

    void deleteProduct(Long id);
}
