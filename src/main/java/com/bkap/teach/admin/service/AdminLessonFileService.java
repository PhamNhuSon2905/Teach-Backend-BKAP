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
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
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
            Path lessonDir = rootPath.resolve(lessonId.toString()).normalize();

            System.out.println(">>> Đang upload vào thư mục: " + lessonDir);

            if (!Files.exists(lessonDir)) {
                Files.createDirectories(lessonDir);
            }

            if (fileType == LessonFileType.PDF) {
                Path pdfPath = lessonDir.resolve(slugName).normalize();

                try (InputStream is = file.getInputStream()) {
                    Files.copy(is, pdfPath, StandardCopyOption.REPLACE_EXISTING);
                }

                long size = 0;
                try {
                    size = fileSizeUtil.size(pdfPath);
                } catch (Exception e) {
                    System.err.println("⚠️ Không tính được size PDF: " + e.getMessage());
                }

                saveLessonFileRecord(
                        lesson,
                        fileType,
                        slugName,
                        "/uploads/lessons/" + lessonId + "/" + slugName,
                        size,
                        null
                );
                return;
            }

            String folderSlug = slugName.replaceAll("(?i)\\.(zip|rar)$", "");
            Path extractDir = lessonDir.resolve(folderSlug).normalize();

            if (!Files.exists(extractDir)) {
                Files.createDirectories(extractDir);
            }

            Path compressedPath = lessonDir.resolve(slugName).normalize();

            try (InputStream is = file.getInputStream()) {
                Files.copy(is, compressedPath, StandardCopyOption.REPLACE_EXISTING);
            }

            if (fileType == LessonFileType.ZIP) {
                unzip(compressedPath, extractDir);
            } else {
                unrar(compressedPath, extractDir);
            }

            normalizeExtractedFolder(extractDir);

            // Chỉ ép nền trắng cho html/body, không đụng vào layout bên trong
            forceWhiteBackgroundForHtmlPackage(extractDir);

            String folderWebPath = "/uploads/lessons/" + lessonId + "/" + folderSlug;

            long folderSize = 0;
            try {
                folderSize = fileSizeUtil.size(extractDir);
            } catch (Exception e) {
                System.err.println("⚠️ Không tính được size Folder: " + e.getMessage());
            }

            saveLessonFileRecord(
                    lesson,
                    fileType,
                    folderSlug,
                    folderWebPath,
                    folderSize,
                    folderWebPath
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi hệ thống: " + e.getMessage(), e);
        }
    }

    private void saveLessonFileRecord(
            Lesson lesson,
            LessonFileType type,
            String name,
            String path,
            long size,
            String folderPath
    ) {
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
        LessonFile file = lessonFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Nội dung bài giảng không tồn tại"));

        Long lessonId = file.getLesson().getId();

        try {
            Path rootDir = Paths.get(lessonUploadDir).toAbsolutePath().normalize();
            Path lessonDir = rootDir.resolve(lessonId.toString()).normalize();

            System.out.println("========== DEBUG DELETE LESSON FILE ==========");
            System.out.println("fileId    = " + fileId);
            System.out.println("lessonId  = " + lessonId);
            System.out.println("fileName  = " + file.getFileName());
            System.out.println("fileType  = " + file.getFileType());
            System.out.println("lessonDir = " + lessonDir);

            if (file.getFileType() == LessonFileType.PDF) {
                Path pdfPath = lessonDir.resolve(file.getFileName()).normalize();
                System.out.println(">>> Xóa PDF: " + pdfPath);
                Files.deleteIfExists(pdfPath);
            } else {
                Path folderPath = lessonDir.resolve(file.getFileName()).normalize();
                System.out.println(">>> Xóa folder bài giảng: " + folderPath);
                deleteDirectoryRecursively(folderPath);

                String ext = (file.getFileType() == LessonFileType.ZIP) ? ".zip" : ".rar";
                Path compressedPath = lessonDir.resolve(file.getFileName() + ext).normalize();
                System.out.println(">>> Xóa file nén: " + compressedPath);
                Files.deleteIfExists(compressedPath);
            }

            lessonFileRepository.delete(file);

            System.out.println("✅ Đã xóa record DB fileId = " + fileId);
            System.out.println("==============================================");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi xóa: " + e.getMessage(), e);
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
        try {
            unzipWithCharset(zipFile, targetDir, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ Không giải nén ZIP bằng UTF-8 được, thử CP437: " + e.getMessage());
            deleteDirectoryRecursively(targetDir);
            Files.createDirectories(targetDir);
            try {
                unzipWithCharset(zipFile, targetDir, Charset.forName("CP437"));
            } catch (IllegalArgumentException ex) {
                System.err.println("⚠️ Không giải nén ZIP bằng CP437 được, thử windows-1258: " + ex.getMessage());
                deleteDirectoryRecursively(targetDir);
                Files.createDirectories(targetDir);
                unzipWithCharset(zipFile, targetDir, Charset.forName("windows-1258"));
            }
        }
    }

    private void unzipWithCharset(Path zipFile, Path targetDir, Charset charset) throws IOException {
        Path safeTargetDir = targetDir.toAbsolutePath().normalize();
        Files.createDirectories(safeTargetDir);

        try (ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(Files.newInputStream(zipFile)), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                if (entryName == null || entryName.trim().isEmpty()) continue;

                entryName = entryName.replace("\\", "/").trim();
                Path newPath = safeTargetDir.resolve(entryName).normalize();

                if (!newPath.startsWith(safeTargetDir)) {
                    throw new IOException("Zip Slip detected: " + entryName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(newPath);
                } else {
                    if (newPath.getParent() != null) Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

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
            indexFile = stream
                    .filter(p -> p.getFileName().toString().equalsIgnoreCase("index.html"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy index.html"));
            indexRoot = indexFile.getParent();
        }

        if (indexRoot.equals(extractDir)) return;

        try (var stream = Files.list(indexRoot)) {
            for (Path source : stream.toList()) {
                Path target = extractDir.resolve(source.getFileName()).normalize();
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
                    moveContent(child, target.resolve(child.getFileName()).normalize());
                }
            }
            deleteDirectoryRecursively(source);
        } else {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Chỉ ép nền trắng cho html và body.
     * KHÔNG đụng vào các element bên trong để tránh vỡ layout flipbook/SCORM.
     */
    private void forceWhiteBackgroundForHtmlPackage(Path extractDir) {
        System.out.println(">>> Ép nền trắng cho HTML5 package: " + extractDir);
        try (var stream = Files.walk(extractDir)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                String fileName = path.getFileName().toString().toLowerCase();
                try {
                    if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                        injectWhiteBackgroundIntoHtml(path);
                    }
                    if (fileName.endsWith(".css")) {
                        appendWhiteBackgroundIntoCss(path);
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Không ép được nền trắng cho: " + path + " | " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("⚠️ Không scan được package HTML5: " + e.getMessage());
        }
    }

    private void injectWhiteBackgroundIntoHtml(Path htmlPath) throws IOException {
        String content = readTextFileSafe(htmlPath);

        String customCss = """
        <style id="lms-force-white-background">
            html,
            body,
            .pageViewer {
                background: #ffffff !important;
                background-color: #ffffff !important;
            }
        </style>
        <script id="lms-force-white-background-script">
            (function () {
                function forceWhitePageViewer() {
                    var els = document.querySelectorAll('.pageViewer');
                    els.forEach(function(el) {
                        el.style.setProperty('background', '#ffffff', 'important');
                        el.style.setProperty('background-color', '#ffffff', 'important');
                    });
                }

                setTimeout(forceWhitePageViewer, 100);
                setTimeout(forceWhitePageViewer, 500);
                setTimeout(forceWhitePageViewer, 1000);
                setTimeout(forceWhitePageViewer, 2000);

                window.addEventListener('load', forceWhitePageViewer);

                if (window.MutationObserver) {
                    var observer = new MutationObserver(forceWhitePageViewer);
                    window.addEventListener('load', function () {
                        if (document.body) {
                            observer.observe(document.body, {
                                childList: true, subtree: true,
                                attributes: true, attributeFilter: ['style', 'class']
                            });
                        }
                    });
                }
            })();
        </script>
        """;

        content = removeOldWhiteBackgroundInjection(content);

        if (content.toLowerCase().contains("</head>")) {
            content = content.replaceFirst("(?i)</head>", customCss + "</head>");
        } else {
            content = customCss + content;
        }

        Files.writeString(htmlPath, content, StandardCharsets.UTF_8);
    }

    private void appendWhiteBackgroundIntoCss(Path cssPath) throws IOException {
        String content = readTextFileSafe(cssPath);

        content = content.replaceAll(
                "(?s)/\\* lms-force-white-background-css-start \\*/.*?/\\* lms-force-white-background-css-end \\*/",
                ""
        );

        // Chỉ ép html và body
        String customCss = """

        /* lms-force-white-background-css-start */
        html,
        body {
            background: #ffffff !important;
            background-color: #ffffff !important;
        }
        /* lms-force-white-background-css-end */
        """;

        Files.writeString(cssPath, content + customCss, StandardCharsets.UTF_8);
    }

    private String removeOldWhiteBackgroundInjection(String content) {
        content = content.replaceAll(
                "(?is)<style[^>]*id=[\"']lms-force-white-background[\"'][^>]*>.*?</style>",
                ""
        );
        content = content.replaceAll(
                "(?is)<script[^>]*id=[\"']lms-force-white-background-script[\"'][^>]*>.*?</script>",
                ""
        );
        return content;
    }

    private String readTextFileSafe(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        try {
            return StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return Charset.forName("windows-1258")
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        }
    }

    private void cleanupEmptyDirs(Path root) throws IOException {
        try (var stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    if (Files.isDirectory(p) && !p.equals(root)) {
                        try (var children = Files.list(p)) {
                            if (children.findAny().isEmpty()) Files.deleteIfExists(p);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("⚠️ Không xóa được thư mục rỗng: " + p + " | " + e.getMessage());
                }
            });
        }
    }

    private void deleteDirectoryRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            System.out.println("⚠️ Không tồn tại thư mục cần xóa: " + root);
            return;
        }
        System.out.println(">>> Bắt đầu xóa thư mục: " + root);
        try (var stream = Files.walk(root)) {
            stream.sorted((a, b) -> b.compareTo(a)).forEach(p -> {
                try {
                    System.out.println("Đang xóa: " + p);
                    Files.deleteIfExists(p);
                    System.out.println("✅ Đã xóa: " + p);
                } catch (IOException e) {
                    throw new RuntimeException("❌ Không xóa được: " + p + " | Lý do: " + e.getMessage(), e);
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) throw (IOException) e.getCause();
            throw e;
        }
    }
}