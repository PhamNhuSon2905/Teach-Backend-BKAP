package com.bkap.teach.admin.service;

import com.bkap.teach.admin.dto.request.AdminChangePasswordRequest;
import com.bkap.teach.admin.dto.request.AdminProfileRequest;
import com.bkap.teach.entity.User;
import com.bkap.teach.repository.UserRepository;
import com.bkap.teach.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${upload.avatar-dir}")
    private String avatarUploadDir;


    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tồn tại hồ sơ cá nhân !"));
    }

    @Transactional
    public void updateProfile(Long userId,
                              AdminProfileRequest dto,
                              MultipartFile avatarFile) {

        User user = getCurrentUser(userId);

        user.setFullname(dto.getFullname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setUpdatedAt(LocalDateTime.now());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            validateAvatar(avatarFile);
            uploadAvatar(user, avatarFile);
        }
        userRepository.save(user);
        UserPrincipal.updateAuthentication(user);
    }

    private void validateAvatar(MultipartFile avatarFile) {


        if (!List.of("image/jpeg", "image/png", "image/jpg")
                .contains(avatarFile.getContentType())) {
            throw new IllegalArgumentException("Chỉ cho phép ảnh JPG, JPEG, PNG !");
        }

        if (avatarFile.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 2MB !");
        }
    }

    private void uploadAvatar(User user, MultipartFile avatarFile) {
        try {
            Path uploadPath = Paths.get(avatarUploadDir);
            Files.createDirectories(uploadPath);

            if (user.getAvatar() != null && !user.getAvatar().contains("default_admin.jpg")) {
                Path oldFile = uploadPath.resolve(Paths.get(user.getAvatar()).getFileName());
                Files.deleteIfExists(oldFile);
            }

            String filename = generateSafeAvatarFilename(avatarFile);
            Path filePath = uploadPath.resolve(filename);
            avatarFile.transferTo(filePath.toFile());

            user.setAvatar("/avatar/" + filename);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu ảnh : " + e.getMessage());
        }
    }


    public void changePassword(Long userId, AdminChangePasswordRequest dto) {
        User user = getCurrentUser(userId);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng !");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp !");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ !");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        UserPrincipal.updateAuthentication(user);
    }

    private String generateSafeAvatarFilename(MultipartFile file) {
        String extension = getFileExtension(file);
        String randomName = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "");

        return "avt_" + randomName + extension;
    }

    private String getFileExtension(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) return ".jpg";

        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            default -> ".jpg";
        };
    }


}
