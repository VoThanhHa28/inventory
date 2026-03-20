package com.project.inventory.exception;

import com.project.inventory.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Đánh dấu đây là "Bảo vệ cổng ra", mọi Exception ném ra đều bị tóm ở đây
public class GlobalExceptionHandler {

    // 1. Bắt lỗi Validation (Lỗi do @Valid ném ra trong DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();

        // Lặp qua từng lỗi và lấy message tiếng Việt ta đã viết trong DTO
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // ĐÓNG GÓI LỖI VÀO ApiResponse
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .code(HttpStatus.BAD_REQUEST.value()) // Mã 400
                .message("Dữ liệu đầu vào không hợp lệ")
                .data(errors) // Nhét chi tiết lỗi của từng field vào phần data
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 2. Bắt lỗi không tìm thấy tài nguyên (ResourceNotFoundException)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.NOT_FOUND.value()) // Mã 404
                .message(ex.getMessage()) // Lấy câu thông báo từ Service (VD: "Không tìm thấy sản phẩm")
                // data sẽ bị null và bị loại bỏ khỏi JSON nhờ @JsonInclude trong ApiResponse
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 3. (CHUẨN SENIOR) Bắt TẤT CẢ các lỗi hệ thống không lường trước được
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // Mã 500
                .message("Lỗi hệ thống: " + ex.getMessage()) // Thực tế đi làm sẽ giấu ex.getMessage() đi, nhưng đang code thì cứ in ra để dễ fix
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}