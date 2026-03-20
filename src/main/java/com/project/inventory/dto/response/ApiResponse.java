package com.project.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Phép màu ở đây: Nếu field nào bị null, JSON sẽ tự động ẩn nó đi cho sạch.
public class ApiResponse<T> {
    private int code;       // VD: 200, 201, 400, 404...
    private String message; // Câu thông báo thân thiện cho Frontend hiển thị
    private T data;         // Chứa DTO hoặc List DTO bên trong
}