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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
           
            Path rootPath = Paths.get(lessonUploadDir).toAbsolutePath().normalize();
            Path lessonDir = rootPath.resolve(lessonId.toString());

            System.out.println(">>> Đang upload vào thư mục: " + lessonDir);

            if (!Files.exists(lessonDir)) {
                Files.createDirectories(lessonDir);
            }

          
            if (fileType == LessonFileType.PDF) {
                Path pdfPath = lessonDir.resolve(slugName);
                
                try (InputStream is = file.getInputStream()) {
                    Files.copy(is, pdfPath, StandardCopyOption.REPLACE_EXISTING);
                }

                long size = 0;
                try {
                     size = fileSizeUtil.size(pdfPath);
                } catch (Exception e) {
                     System.err.println("⚠️ Không tính được size PDF: " + e.getMessage());
                }

                saveLessonFileRecord(lesson, fileType, slugName, "/uploads/lessons/" + lessonId + "/" + slugName, size, null);
                return;
            }

          
            String folderSlug = slugName.replaceAll("\\.(zip|rar)$", "");
            Path extractDir = lessonDir.resolve(folderSlug);

            if (!Files.exists(extractDir)) Files.createDirectories(extractDir);

            Path compressedPath = lessonDir.resolve(slugName);
            
            try (InputStream is = file.getInputStream()) {
                Files.copy(is, compressedPath, StandardCopyOption.REPLACE_EXISTING);
            }

            if (fileType == LessonFileType.ZIP) {
                unzip(compressedPath, extractDir);
            } else {
                unrar(compressedPath, extractDir);
            }

            normalizeExtractedFolder(extractDir);

            String folderWebPath = "/uploads/lessons/" + lessonId + "/" + folderSlug;
            
            long folderSize = 0;
            try {
                folderSize = fileSizeUtil.size(extractDir);
            } catch (Exception e) {
                System.err.println("⚠️ Không tính được size Folder: " + e.getMessage());
            }

            saveLessonFileRecord(lesson, fileType, folderSlug, folderWebPath, folderSize, folderWebPath);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi hệ thống: " + e.getMessage(), e);
        }
    }

    
    private void saveLessonFileRecord(Lesson lesson, LessonFileType type, String name, String path, long size, String folderPath) {
        LessonFile lf = new LessonFile();
        lf.setLesson(lesson);
        lf.setFileType(type);
        lf.setFileName(name);
        lf.setFilePath(path);
        lf.setFileSize(size);
        lf.setFolderPath(folderPath);
        lf.setIsRoot(true);
        lessonFileRepository.save(lf);
    }

    public void deleteLessonFile(Long fileId) {
        LessonFile file = lessonFileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("Lỗi"));
        Long lessonId = file.getLesson().getId();
        try {
          
            Path rootDir = Paths.get(lessonUploadDir).toAbsolutePath().normalize();
            Path lessonDir = rootDir.resolve(lessonId.toString());

            if (file.getFileType() == LessonFileType.PDF) {
                Files.deleteIfExists(lessonDir.resolve(file.getFileName()));
            } else {
                Path folderPath = lessonDir.resolve(file.getFileName());
                deleteDirectoryRecursively(folderPath);
                String ext = (file.getFileType() == LessonFileType.ZIP) ? ".zip" : ".rar";
                Files.deleteIfExists(lessonDir.resolve(file.getFileName() + ext));
            }
            lessonFileRepository.delete(file);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xóa: " + e.getMessage());
        }
    }

  

    public LessonFile findById(Long fileId) {
        return lessonFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Nội dung bài giảng không tồn tại !"));
    }

    public List<LessonFile> findByLesson(Long lessonId) {
        return lessonFileRepository.findByLessonId(lessonId);
    }

    private LessonFileType detectType(String name) {
        name = name.toLowerCase();
        if (name.endsWith(".pdf")) return LessonFileType.PDF;
        if (name.endsWith(".zip")) return LessonFileType.ZIP;
        if (name.endsWith(".rar")) return LessonFileType.RAR;
        throw new RuntimeException("Không hỗ trợ định dạng tệp vừa tải !");
    }

    private void unzip(Path zipFile, Path targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newPath = targetDir.resolve(entry.getName()).normalize();
                if (!newPath.startsWith(targetDir)) throw new IOException("Zip Slip detected");

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void unrar(Path rarFile, Path targetDir) throws IOException {
        try (Archive archive = new Archive(rarFile.toFile())) {
            FileHeader header;
            while ((header = archive.nextFileHeader()) != null) {
                String rawName = header.isUnicode() ? header.getFileNameW() : header.getFileNameString();
                rawName = rawName.replace("\\", "/").trim();
                if (rawName.isEmpty()) continue;

                Path outPath = targetDir.resolve(rawName).normalize();
                if (!outPath.startsWith(targetDir)) throw new IOException("Rar Slip detected");

                if (header.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    if (outPath.getParent() != null) Files.createDirectories(outPath.getParent());
                    try (OutputStream os = Files.newOutputStream(outPath)) {
                        archive.extractFile(header, os);
                    }
                }
            }
        } catch (RarException e) {
            throw new IOException("Lỗi giải nén RAR", e);
        }
    }

    private void normalizeExtractedFolder(Path extractDir) throws IOException {
        Path indexFile;
        Path indexRoot;
        try (var stream = Files.walk(extractDir, 10)) {
            indexFile = stream.filter(p -> p.getFileName().toString().equalsIgnoreCase("index.html")).findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy index.html"));
            indexRoot = indexFile.getParent();
        }
        overrideHtmlBackground(indexFile);
        if (indexRoot.equals(extractDir)) return;
        try (var stream = Files.list(indexRoot)) {
            for (Path source : stream.toList()) {
                Path target = extractDir.resolve(source.getFileName());
                if (source.equals(target)) continue;
                moveContent(source, target);
            }
        }
        cleanupEmptyDirs(extractDir);
    }

    private void moveContent(Path source, Path target) throws IOException {
        if (Files.isDirectory(source)) {
            if (!Files.exists(target)) Files.createDirectories(target);
            try (var stream = Files.list(source)) {
                for (Path child : stream.toList()) {
                    moveContent(child, target.resolve(child.getFileName()));
                }
            }
            deleteDirectoryRecursively(source); 
        } else {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void overrideHtmlBackground(Path indexHtmlPath) {
        try {
            String content = Files.readString(indexHtmlPath, StandardCharsets.UTF_8);
            String customCss = "<style>html, body { background-color: rgba(243, 236, 236, 0) !important; }</style>";
            content = content.contains("</head>") ? content.replace("</head>", customCss + "</head>") : customCss + content;
            Files.writeString(indexHtmlPath, content, StandardCharsets.UTF_8);
        } catch (Exception ignored) {}
    }

    private void cleanupEmptyDirs(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    if (Files.isDirectory(p) && !p.equals(root) && Files.list(p).findAny().isEmpty()) Files.delete(p);
                } catch (IOException ignored) {}
            });
        }
    }

    private void deleteDirectoryRecursively(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (var stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }
}