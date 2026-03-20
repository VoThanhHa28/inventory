package com.project.inventory.controller;


import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file){
        try {
            // 1. Lấy tên file gốc (ví dụ: avatar.png)
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());

            // 2. Tạo tên file mới để tránh trùng lặp (ví dụ: 550e8400-e29b...png)
            // UUID là chuỗi ngẫu nhiên duy nhất trên thế giới
            String newFileName = UUID.randomUUID().toString() + "_" + fileName;

            // 3. Tạo đường dẫn lưu file (Folder uploads + Tên file mới)
            Path uploadPath = Paths.get(UPLOAD_DIR);

            // Nếu folder chưa tồn tại thì tạo mới
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 4. Lưu file vào ổ cứng (Copy file từ RAM vào ổ cứng)
            Path filePath = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 5. Trả về đường dẫn URL để Frontend hiển thị
            String fileUrl = "/uploads/" + newFileName;
            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Lỗi upload file: " + e.getMessage());
        }
    }

}
