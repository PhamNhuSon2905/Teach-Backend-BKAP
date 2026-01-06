package com.bkap.teach.admin.service;

import com.bkap.teach.entity.Lesson;
import com.bkap.teach.enums.LessonStatus;
import com.bkap.teach.repository.lession.LessonFileRepository;
import com.bkap.teach.repository.lession.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminLessonService {

    private final LessonRepository lessonRepository;

    private final LessonFileRepository lessonFileRepository;

    @Value("${upload.lesson-cover-dir}")
    private String lessonCoverUploadDir;

    private static final String DEFAULT_COVER =
            "/assets/images/default_lesson.png";

    // search and pagination
    public Page<Lesson> findAll(int pageNo, int pageSize, String keyword) {

        PageRequest pageable = PageRequest.of(
                pageNo - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        if (keyword == null || keyword.trim().isEmpty()) {
            return lessonRepository.findAll(pageable);
        }

        return lessonRepository
                .findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                        keyword.trim(),
                        keyword.trim(),
                        pageable
                );
    }

    // tim theo id
    public Lesson findById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Bài học không tồn tại: " + id)
                );
    }
    // tao bai giang
    public Lesson create(Lesson lesson) {
        lesson.setId(null);
        lesson.setLessonStatus(LessonStatus.ACTIVE);

        if (lesson.getCoverImage() == null) {
            lesson.setCoverImage(DEFAULT_COVER);
        }

        return lessonRepository.save(lesson);
    }

    //update bai giang
    public Lesson update(Long id, Lesson data) {
        Lesson lesson = findById(id);

        lesson.setCode(data.getCode());
        lesson.setName(data.getName());
        lesson.setGrade(data.getGrade());
        lesson.setTeachingMonth(data.getTeachingMonth());
        lesson.setDescription(data.getDescription());
        lesson.setLessonStatus(data.getLessonStatus());

        return lessonRepository.save(lesson);
    }


    // xoa bai giang
    public void delete(Long id) {
        Lesson lesson = findById(id);
        long contentCount = lessonFileRepository.countByLessonId(id);
        if (contentCount > 0) {
            throw new RuntimeException(
                    "Không thể xoá bài giảng vì vẫn còn nội dung !   " +
                            "Vui lòng xoá toàn bộ nội dung bài giảng trước."
            );
        }
        if (lesson.getCoverImage() != null
                && !lesson.getCoverImage().equals(DEFAULT_COVER)) {

            try {
                Path uploadPath = Paths.get(lessonCoverUploadDir);
                Path filePath = uploadPath.resolve(
                        Paths.get(lesson.getCoverImage()).getFileName()
                );
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                throw new RuntimeException("Không thể xoá ảnh bìa bài giảng !");
            }
        }
        lessonRepository.delete(lesson);
    }

    // validation null file anh bia bai giang
    public Lesson handleCoverImage(Lesson lesson, MultipartFile coverFile) {
        if (coverFile == null || coverFile.isEmpty()) {
            return lesson;
        }
        validateCoverImage(coverFile);
        uploadCoverImage(lesson, coverFile);

        return lesson;
    }

    // validation size type file anh bia
    private void validateCoverImage(MultipartFile file) {

        if (!List.of("image/jpeg", "image/png", "image/jpg")
                .contains(file.getContentType())) {
            throw new IllegalArgumentException("Ảnh bìa bài giảng chỉ cho phép JPG, JPEG, PNG!");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh bìa bài giảng không được vượt quá 2MB!");
        }
    }

    // upload anh len
    private void uploadCoverImage(Lesson lesson, MultipartFile coverFile) {

        try {
            Path uploadPath = Paths.get(lessonCoverUploadDir);
            Files.createDirectories(uploadPath);

            if (lesson.getCoverImage() != null
                    && !lesson.getCoverImage().equals(DEFAULT_COVER)) {

                Path oldFile = uploadPath.resolve(
                        Paths.get(lesson.getCoverImage()).getFileName()
                );
                Files.deleteIfExists(oldFile);
            }

            String filename = generateSafeLessonCoverFilename(coverFile);
            Path filePath = uploadPath.resolve(filename);
            coverFile.transferTo(filePath.toFile());

            lesson.setCoverImage("/lesson-covers/" + filename);
            lesson.setUpdatedAt(LocalDateTime.now());

            lessonRepository.save(lesson);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu ảnh bìa bài học: " + e.getMessage());
        }
    }


    // neu ko co anh thi lay anh mac dinh
    private void setDefaultCoverIfMissing(Lesson lesson) {

        if (lesson.getCoverImage() == null || lesson.getCoverImage().isBlank()) {
            lesson.setCoverImage(DEFAULT_COVER);
            lessonRepository.save(lesson);
        }
    }

    // chuan hoa file anh truoc khi luu vao uploads/lesson cover
    private String generateSafeLessonCoverFilename(MultipartFile file) {
        String extension = getFileExtension(file);
        String randomName = UUID.randomUUID()
                .toString()
                .replace("-", "");

        return "lsc_" + randomName + extension;
    }


    // tao duoi file gan vao file anh khi xay ra van de
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
