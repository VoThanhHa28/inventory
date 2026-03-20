package com.project.inventory.service;

import com.project.inventory.dto.ProductRequestDTO;
import com.project.inventory.dto.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductResponseDTO createProduct(ProductRequestDTO request);

    Page<ProductResponseDTO> getAllProducts(Pageable pageable);

    ProductResponseDTO getProductById(Long id);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO request);

    void deleteProduct(Long id);
}
