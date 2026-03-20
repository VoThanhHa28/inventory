package com.project.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequestDTO {

    @NotBlank(message = "Tên sản phẩm không được để trống!") // Không được null, không được ""
    private String name;

    @NotNull(message =  "Giá sản phẩm không được để trống!")
    @Min(value = 0, message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    private Double price;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng kho phải lớn hơn hoặc bằng 0")
    private Integer stockQuantity;

    private String image;
}
