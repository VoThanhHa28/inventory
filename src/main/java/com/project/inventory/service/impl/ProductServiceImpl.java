package com.project.inventory.service.impl;

import com.project.inventory.dto.product.ProductRequestDTO;
import com.project.inventory.dto.product.ProductResponseDTO;
import com.project.inventory.entity.Product;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.project.inventory.exception.ResourceNotFoundException;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        // 1. Chuyển đổi DTO -> Entity (Mapping)
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .image(request.getImage())
                .build();

        // 2. Lưu xuống DB
        Product savedProduct = productRepository.save(product);

        // 3. Chuyển đổi ngược Entity -> ResponseDto để trả về
        return mapToResponseDTO(savedProduct);

    }

    @Override
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        // Lấy list Entity từ DB
        Page<Product> productPage = productRepository.findAll(pageable);

        // Dùng Stream API để duyệt qua từng thằng và map sang DTO
        return productPage.map(this::mapToResponseDTO);
    }

    @Override
    public ProductResponseDTO getProductById(Long id) {
        // Cách viết chuẩn:
        Product product = productRepository.findById(id)
                // .orElseThrow: Nếu tìm thấy thì lấy Product ra, nếu không thấy thì ném lỗi
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id: " + id));

        return mapToResponseDTO(product);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
        Product existingProduct = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm id: " + id));

        existingProduct.setName(request.getName());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setImage(request.getImage());

        // Lưu đè lại (JPA thông minh: Có ID rồi thì save = update)
        Product updatedProduct = productRepository.save(existingProduct);

        return mapToResponseDTO(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        if(!productRepository.existsById(id)){
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm id: " + id);
        }
        productRepository.deleteById(id);
    }

    // Hàm phụ trợ để map Entity sang DTO
    private ProductResponseDTO mapToResponseDTO(Product product){
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .image(product.getImage())
                .build();
    }
}
