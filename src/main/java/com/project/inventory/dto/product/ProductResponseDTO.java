package com.project.inventory.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ProductResponseDTO - API response for product information
 */
@Data
@Builder
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;
    private String imageUrl;
    private String category;
    private Boolean isDeleted;
    @JsonIgnore
    private LocalDateTime createdAt;
    @JsonIgnore
    private LocalDateTime updatedAt;
}
