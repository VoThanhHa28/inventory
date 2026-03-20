package com.project.inventory.exception;

// Kế thừa RuntimeException để Java hiểu đây là một lỗi có thể ném ra (throw)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message); // Truyền thông báo lỗi lên cho lớp cha xử lý
    }
}