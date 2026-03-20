package com.project.inventory.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // Cấu hình: Khi gõ http://localhost:8080/uploads/tên_file.jpg
        // -> Nó sẽ chọc vào thư mục "uploads" nằm ở gốc dự án để lấy file ra.
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
