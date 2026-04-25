package com.bkap.teach.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    // Lấy đường dẫn gốc từ file yml (C:/apps/lms.bkapai.vn/uploads)
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        
        // Chuẩn hóa đường dẫn để Spring hiểu đây là file hệ thống
        
        String rootUploadPath = "file:///" + uploadDir;
        if (!rootUploadPath.endsWith("/")) {
            rootUploadPath += "/";
        }

        logger.info("🌍 CONFIG DETECTED: Phục vụ file từ đường dẫn: {}", rootUploadPath);

        // Map đường dẫn URL /uploads/** vào thư mục vật lý
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(rootUploadPath)
                .setCachePeriod(0); 

        // Map cho lessons
        registry.addResourceHandler("/lessons/**")
                .addResourceLocations(rootUploadPath + "lessons/")
                .setCachePeriod(0);

        // Map cho avatar
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations(rootUploadPath + "avatar/")
                .setCachePeriod(0);
        
        // Map cho lesson-covers
        registry.addResourceHandler("/lesson-covers/**")
                .addResourceLocations(rootUploadPath + "lesson-covers/")
                .setCachePeriod(0);
        
        logger.info("✅ Static resources đã được cấu hình trỏ về: {}", uploadDir);
        
        
    }
}