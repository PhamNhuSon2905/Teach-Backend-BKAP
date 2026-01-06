package com.bkap.teach.service.teacher;

import com.bkap.teach.entity.User;
import com.bkap.teach.repository.UserRepository;
import com.bkap.teach.security.UserPrincipal;
import com.bkap.teach.teacher.dto.request.TeacherUpdateProfileRequest;
import com.bkap.teach.teacher.dto.response.TeacherProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class TeacherProfileService {

    @Autowired
    private UserRepository userRepository;

    @Value("${upload.avatar-dir}")
    private String avatarUploadDir;

    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tồn tại hồ sơ giảng viên !"));
    }

    @Transactional
    public TeacherProfileResponse updateProfile(
            Long userId,
            TeacherUpdateProfileRequest req,
            MultipartFile avatarFile
    ) {

        User user = getCurrentUser(userId);

        if (req.getEmail() != null && !req.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng !");
            }
            user.setEmail(req.getEmail());
        }
        user.setFullname(req.getFullname());
        user.setPhone(req.getPhone());
        user.setAddress(req.getAddress());
        user.setUpdatedAt(LocalDateTime.now());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            validateAvatar(avatarFile);
            uploadAvatar(user, avatarFile);
        }

        userRepository.save(user);
        UserPrincipal.updateAuthentication(user);

        return toProfileResponse(user);
    }

    public TeacherProfileResponse toProfileResponse(User user) {
        TeacherProfileResponse res = new TeacherProfileResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setFullname(user.getFullname());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setAddress(user.getAddress());
        res.setRole(user.getRole().name());
        res.setStatus(user.getStatus().name());
        res.setAvatar(
                (user.getAvatar() != null && !user.getAvatar().isBlank())
                        ? user.getAvatar()
                        : "/assets/images/default_teacher.jpg"
        );
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());
        return res;
    }


    private void validateAvatar(MultipartFile avatarFile) {

        if (!List.of("image/jpeg", "image/png", "image/jpg")
                .contains(avatarFile.getContentType())) {
            throw new IllegalArgumentException(
                    "Chỉ cho phép ảnh JPG, JPEG, PNG !");
        }

        if (avatarFile.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "Ảnh đại diện không được vượt quá 2MB !");
        }
    }

    private void uploadAvatar(User user, MultipartFile avatarFile) {

        try {
            Path uploadPath = Paths.get(avatarUploadDir);
            Files.createDirectories(uploadPath);

            if (user.getAvatar() != null
                    && !user.getAvatar().contains("default_teacher.jpg")) {

                Path oldFile = uploadPath.resolve(
                        Paths.get(user.getAvatar()).getFileName()
                );
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

    private String generateSafeAvatarFilename(MultipartFile file) {
        String extension = getFileExtension(file);
        String randomName = java.util.UUID.randomUUID()
                .toString()
                .replace("-", "");
        return "teacher_" + randomName + extension;
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
