package com.project.inventory.repository;

import com.project.inventory.entity.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testCreateAndFindProduct(){
        // 1. Giả lập dữ liệu
        Product product = Product.builder()
                .name("Iphone 15 Pro Max")
                .price(3000000.0)
                .stockQuantity(100)
                .build();

        // 2. Thực hiện lưu
        Product savedProduct = productRepository.save(product);

        // 3. Kiểm tra kết quả
        Assertions.assertNotNull(savedProduct.getId()); // Lưu xong phải có id
        Assertions.assertEquals("Iphone 15 Pro Max", savedProduct.getName()); // Tên phải khớp

        System.out.println(">>> TEST SUCCESS: Product ID is " + savedProduct.getId());
    }
}
