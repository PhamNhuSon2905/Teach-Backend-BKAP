package com.bkap.teach.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${upload.avatar-dir}")
    private String avatarDir;

    @Value("${upload.lesson-cover-dir}")
    private String lessonCoverDir;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${upload.lesson-dir}")
    private String lessonDir;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @PostConstruct
    public void init() {
        logger.info("=================================================");
        logger.info("Initializing file storage configuration");
        logger.info("Active Profile: {}", activeProfile);
        logger.info("=================================================");

        // Tạo các thư mục cần thiết
        createDirectoryIfNotExists(uploadDir, "Upload Root");
        createDirectoryIfNotExists(avatarDir, "Avatar");
        createDirectoryIfNotExists(lessonCoverDir, "Lesson Covers");
        createDirectoryIfNotExists(lessonDir, "Lessons");

        logger.info("=================================================");
        logger.info("File storage initialized successfully!");
        logger.info("=================================================");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Avatar handler
        String avatarLocation = "file:" + ensureTrailingSlash(avatarDir);
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations(avatarLocation)
                .setCachePeriod(0);
        logger.debug("Mapped /avatar/** to {}", avatarLocation);

        // Lesson cover handler
        String lessonCoverLocation = "file:" + ensureTrailingSlash(lessonCoverDir);
        registry.addResourceHandler("/lesson-covers/**")
                .addResourceLocations(lessonCoverLocation)
                .setCachePeriod(0);
        logger.debug("Mapped /lesson-covers/** to {}", lessonCoverLocation);

        String lessonLocation = "file:" + ensureTrailingSlash(lessonDir);
        registry.addResourceHandler("/lessons/**")
                .addResourceLocations(lessonLocation)
                .setCachePeriod(0);
        logger.debug("Mapped /lessons/** to {}", lessonLocation);


        // General uploads handler
        String uploadLocation = "file:" + ensureTrailingSlash(uploadDir);
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(0);
        logger.debug("Mapped /uploads/** to {}", uploadLocation);
    }

    private String ensureTrailingSlash(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }

        // Normalize path separators
        path = path.replace("\\", File.separator).replace("/", File.separator);

        return path.endsWith(File.separator) ? path : path + File.separator;
    }

    /**
     * Tạo thư mục nếu chưa tồn tại
     */
    private void createDirectoryIfNotExists(String pathString, String description) {
        try {
            Path path = Paths.get(pathString);
            File directory = path.toFile();

            if (!directory.exists()) {
                Files.createDirectories(path);
                logger.info("Created {} directory: {}", description, path.toAbsolutePath());
            } else {
                logger.info("{} directory exists: {}", description, path.toAbsolutePath());
            }

            // Kiểm tra quyền ghi
            if (!directory.canWrite()) {
                logger.warn("WARNING: No write permission for {} directory: {}",
                        description, path.toAbsolutePath());
            }

            // Log số lượng files
            String[] files = directory.list();
            if (files != null) {
                logger.debug("   Contains {} files/folders", files.length);
            }

        } catch (Exception e) {
            logger.error("Failed to create {} directory: {}", description, pathString, e);
            throw new RuntimeException("Cannot initialize " + description + " directory: " + pathString, e);
        }
    }
}