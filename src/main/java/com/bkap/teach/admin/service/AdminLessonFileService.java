package com.bkap.teach.admin.service;

import com.bkap.teach.entity.Lesson;
import com.bkap.teach.entity.LessonFile;
import com.bkap.teach.enums.LessonFileType;
import com.bkap.teach.repository.lession.LessonFileRepository;
import com.bkap.teach.repository.lession.LessonRepository;
import com.bkap.teach.utils.FileSizeUtil;
import com.bkap.teach.utils.SlugUtil;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class AdminLessonFileService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    @Value("${upload.lesson-dir}")
    private String lessonUploadDir;

    @Autowired
    private LessonFileRepository lessonFileRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private FileSizeUtil fileSizeUtil;

    public LessonFile findById(Long fileId) {
        return lessonFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Nội dung bài giảng không tồn tại !"));
    }

    // tim bai giang theo id
    public List<LessonFile> findByLesson(Long lessonId) {
        return lessonFileRepository.findByLessonId(lessonId);
    }

    // upload va validation file and folder
    public void upload(Long lessonId, MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn tệp bài giảng cần tải lên !");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("Tệp vượt quá 50MB ! Vui lòng thử lại.");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Bài giảng không tồn tại !"));

        String originalName = Objects.requireNonNull(file.getOriginalFilename());
        String slugName = SlugUtil.toSlugFilename(originalName);
        LessonFileType fileType = detectType(slugName);

        try {
            Path lessonDir = Paths.get(lessonUploadDir, lessonId.toString());
            Files.createDirectories(lessonDir);

            /* ========== PDF ========== */
            if (fileType == LessonFileType.PDF) {
                Path pdfPath = lessonDir.resolve(slugName);
                file.transferTo(pdfPath.toFile());

                long size = fileSizeUtil.size(pdfPath);

                LessonFile lf = new LessonFile();
                lf.setLesson(lesson);
                lf.setFileType(LessonFileType.PDF);
                lf.setFileName(slugName);
                lf.setFilePath("/uploads/lessons/" + lessonId + "/" + slugName);
                lf.setFileSize(size);
                lf.setIsRoot(true);

                lessonFileRepository.save(lf);
                return;
            }

            String folderSlug = slugName.replaceAll("\\.(zip|rar)$", "");
            Path extractDir = lessonDir.resolve(folderSlug);
            Files.createDirectories(extractDir);

            Path compressedPath = lessonDir.resolve(slugName);
            file.transferTo(compressedPath.toFile());

            if (fileType == LessonFileType.ZIP) {
                unzip(compressedPath, extractDir);
            } else {
                unrar(compressedPath, extractDir);
            }

            // chuan hoa
            normalizeExtractedFolder(extractDir);

            Files.deleteIfExists(compressedPath);

            String folderPath = "/uploads/lessons/" + lessonId + "/" + folderSlug;

            long folderSize = fileSizeUtil.size(extractDir);

            LessonFile lf = new LessonFile();
            lf.setLesson(lesson);
            lf.setFileType(fileType);
            lf.setFileName(folderSlug);
            lf.setFilePath(folderPath);
            lf.setFileSize(folderSize);
            lf.setFolderPath(folderPath);
            lf.setIsRoot(true);

            lessonFileRepository.save(lf);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Tải lên & giải nén bài giảng thất bại: " + e.getMessage(), e);
        }
    }

    // kiem tra duoi file
    private LessonFileType detectType(String name) {
        name = name.toLowerCase();
        if (name.endsWith(".pdf")) return LessonFileType.PDF;
        if (name.endsWith(".zip")) return LessonFileType.ZIP;
        if (name.endsWith(".rar")) return LessonFileType.RAR;
        throw new RuntimeException("Không hỗ trợ định dạng tệp vừa tải !");
    }

    // giai nen zip
    private void unzip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(zipFile)))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = targetDir.resolve(entry.getName()).normalize();
                if (!newPath.startsWith(targetDir)) {
                    throw new IOException("Tệp zip không an toàn có dấu hiệu xâm nhập !");
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    //  giai nen rar 4
    private void unrar(Path rarFile, Path targetDir) throws IOException {
        try (Archive archive = new Archive(rarFile.toFile())) {
            FileHeader header;
            while ((header = archive.nextFileHeader()) != null) {

                String rawName = header.isUnicode()
                        ? header.getFileNameW()
                        : header.getFileNameString();

                rawName = rawName.replace("\\", "/").trim();
                if (rawName.isEmpty()) continue;

                Path outPath = targetDir.resolve(rawName).normalize();
                if (!outPath.startsWith(targetDir)) {
                    throw new IOException("Tệp rar không an toàn có dấu hiệu xâm nhập !");
                }

                if (header.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (OutputStream os = Files.newOutputStream(outPath)) {
                        archive.extractFile(header, os);
                    }
                }
            }
        } catch (RarException e) {
            throw new IOException("Không thể mở tệp RAR", e);
        }
    }

    //NORMALIZE (CORE LOGIC)
    private void normalizeExtractedFolder(Path extractDir) throws IOException {

        // Tim thu muc chua index.html
        Path indexRoot;
        try (var stream = Files.walk(extractDir, 5)) {
            indexRoot = stream
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("index.html"))
                    .map(Path::getParent)
                    .findFirst()
                    .orElseThrow(() ->
                            new RuntimeException("Không tìm thấy index.html trong bài giảng"));
        }

        if (indexRoot.equals(extractDir)) return;

        try (var stream = Files.list(indexRoot)) {
            for (Path item : stream.toList()) {
                Path target = extractDir.resolve(item.getFileName());
                if (!Files.exists(target)) {
                    Files.move(item, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        cleanupEmptyDirs(extractDir);
    }
    // lam sach o dia
    private void cleanupEmptyDirs(Path root) throws IOException {
        Files.walk(root)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try {
                        if (Files.isDirectory(p) && Files.list(p).findAny().isEmpty()) {
                            Files.delete(p);
                        }
                    } catch (IOException ignored) {
                    }
                });
    }

    // xoa file PDF và thu muc sau khi upload len
    public void deleteLessonFile(Long fileId) {

        LessonFile file = lessonFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Nội dung bài giảng không tồn tại"));

        Long lessonId = file.getLesson().getId();

        try {

            if (file.getFileType() == LessonFileType.PDF) {
                Path pdfPath = Paths.get(
                        lessonUploadDir,
                        lessonId.toString(),
                        file.getFileName()
                );

                Files.deleteIfExists(pdfPath);

            } else {
                Path folderPath = Paths.get(
                        lessonUploadDir,
                        lessonId.toString(),
                        file.getFileName()
                );

                deleteDirectoryRecursively(folderPath);
            }

            lessonFileRepository.delete(file);

        } catch (Exception e) {
            throw new RuntimeException("Không thể xoá nội dung bài giảng");
        }
    }
    // xoa tuan tu tu file con toi file cha
    private void deleteDirectoryRecursively(Path root) throws IOException {

        if (!Files.exists(root)) return;

        Files.walk(root)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {}
                });
    }


}
