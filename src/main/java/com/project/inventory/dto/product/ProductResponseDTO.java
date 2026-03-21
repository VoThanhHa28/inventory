package com.project.inventory.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private Double price;
    private Integer stockQuantity;
    private String image;
}
