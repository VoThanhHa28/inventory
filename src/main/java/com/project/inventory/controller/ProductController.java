package com.project.inventory.controller;

import com.project.inventory.dto.product.ProductRequestDTO;
import com.project.inventory.dto.product.ProductResponseDTO;
import com.project.inventory.dto.response.ApiResponse;
import com.project.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // 1. Báo đây là nơi tiếp nhận API
@RequestMapping("api/products") // 2. Đường dẫn chung: http://localhost:8080/api/products
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    // @Valid: Ra lệnh cho Spring "Hãy kiểm tra DTO này theo luật tôi vừa viết"
    // Nếu sai luật -> Ném lỗi ngay lập tức, không cho chạy vào trong hàm.
    public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(@Valid @RequestBody ProductRequestDTO request){
        ProductResponseDTO newProduct = productService.createProduct(request);

        ApiResponse<ProductResponseDTO> response = ApiResponse.<ProductResponseDTO>builder()
                .code(HttpStatus.CREATED.value())
                .message("Thêm mới sản phẩm thành công")
                .data(newProduct)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        // 1. Giữ nguyên logic xử lý Pageable & Sort cực chuẩn của bạn
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 2. Lấy dữ liệu từ Service (giữ nguyên)
        Page<ProductResponseDTO> products = productService.getAllProducts(pageable);

        // 3. CHỈ THÊM BƯỚC NÀY: Đóng gói vào hộp ApiResponse
        ApiResponse<Page<ProductResponseDTO>> response = ApiResponse.<Page<ProductResponseDTO>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách sản phẩm thành công")
                .data(products)
                .build();

        return ResponseEntity.ok(response);
    }


    // GET /api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id){
        ProductResponseDTO getProductById = productService.getProductById(id);

        ApiResponse<ProductResponseDTO> response = ApiResponse.<ProductResponseDTO>builder()
                .code(200)
                .message("Lấy thông tin sản phẩm thành công")
                .data(getProductById)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO request){
        ProductResponseDTO updateProduct = productService.updateProduct(id, request);

        ApiResponse<ProductResponseDTO> response = ApiResponse.<ProductResponseDTO>builder()
                .code(200)
                .message("Cập nhật thông tin sản phẩm thành công")
                .data(updateProduct)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
