package com.project.inventory.service;

import com.project.inventory.dto.ProductRequestDTO;
import com.project.inventory.dto.ProductResponseDTO;
import com.project.inventory.entity.Product;
import com.project.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock // 1. Tạo Repository giả
    private ProductRepository productRepository;

    @InjectMocks // 2. Tiêm giả vào thật
    private ProductServiceImpl productService;

    @Test
    public void testCreateProduct_Success() {
        // --GIVEN (Chuẩn bị dữ liệu giả)---
        ProductRequestDTO request = new ProductRequestDTO();
        request.setName("Laptop Dell");
        request.setPrice(15000000.0);
        request.setStockQuantity(10);

        // Tạo ra Entity giả mà DB sẽ trả về (có ID)
        Product savedProduct = Product.builder()
                .name("Laptop Dell")
                .price(15000000.0)
                .stockQuantity(10)
                .build();
        savedProduct.setId(1L);

        // Dạy cho Mock biết phải làm gì
        // Cụ thể là khi ai đó gọi save(), thì hãy trả về savedProduct ngay lập tức
        Mockito.when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // --- WHEN (thực hiện hành động)
        ProductResponseDTO response = productService.createProduct(request);

        // --- THEN (kiểm tra kết quả)
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("Laptop Dell", response.getName());

        System.out.println(">>> TEST SERVICE SUCCESS: Created Product ID " + response.getId());
    }
}
