package com.project.inventory.service;

import com.project.inventory.dto.product.ProductRequestDTO;
import com.project.inventory.dto.product.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request);

    Page<ProductResponseDTO> getAllProducts(Pageable pageable);

    List<ProductResponseDTO> getAllActiveProducts();

    Page<ProductResponseDTO> getProductsByFilters(String category, String search, Double minPrice, Double maxPrice, Pageable pageable);

    List<String> getCategories();

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO request);

    void deleteProduct(Long id);
}
