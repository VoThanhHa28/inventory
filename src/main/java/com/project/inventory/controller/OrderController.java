package com.project.inventory.controller;

import com.project.inventory.dto.response.ApiResponse;
import com.project.inventory.dto.order.OrderRequestDTO;
import com.project.inventory.dto.order.OrderResponseDTO;
import com.project.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Các API quản lý Đơn hàng và Tồn kho")
public class OrderController {

    // Đây chính là Loose Coupling bằng DI (Khai báo Interface, Spring tự Inject Impl)
    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Tạo đơn hàng mới", description = "API tạo đơn hàng, tự động tính tiền và trừ tồn kho (sử dụng Atomic Update chống Race Condition)")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> placeOrder(@Valid @RequestBody OrderRequestDTO requestDTO) {
        OrderResponseDTO response = orderService.placeOrder(requestDTO);

        ApiResponse<OrderResponseDTO> apiResponse = ApiResponse.<OrderResponseDTO>builder()
                .code(HttpStatus.CREATED.value())
                .message("Đặt hàng thành công!")
                .data(response)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }
}