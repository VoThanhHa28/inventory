package com.project.inventory.controller;

import com.project.inventory.dto.ProductRequestDTO;
import com.project.inventory.dto.ProductResponseDTO;
import com.project.inventory.entity.Product;
import com.project.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // 1. Báo đây là nơi tiếp nhận API
@RequestMapping("api/products") // 2. Đường dẫn chung: http://localhost:8080/api/products
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    // @Valid: Ra lệnh cho Spring "Hãy kiểm tra DTO này theo luật tôi vừa viết"
    // Nếu sai luật -> Ném lỗi ngay lập tức, không cho chạy vào trong hàm.
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO request){
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,      // Trang số mấy (Bắt đầu từ 0)
            @RequestParam(defaultValue = "10") int size,     // Lấy bao nhiêu cái
            @RequestParam(defaultValue = "id") String sortBy, // Sắp xếp theo cột nào
            @RequestParam(defaultValue = "asc") String sortDir // Tăng dần (asc) hay giảm dần (desc)
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    // GET /api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO request){
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
