package com.bkap.teach.utils;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component("fileSize")
public class FileSizeUtil {

    // tinh toan kich thuoc file va folder khi upload len
    public long size(Path path) {
        if (path == null || !Files.exists(path)) {
            return 0L;
        }

        try {
            // tep
            if (Files.isRegularFile(path)) {
                return Files.size(path);
            }

            // thu muc
            return Files.walk(path)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();

        } catch (IOException e) {
            return 0L;
        }
    }

    // view hien thi kich thuoc
    public String readable(Long bytes) {
        if (bytes == null || bytes <= 0) return "0 KB";

        double size = bytes;
        if (size < 1024) return (long) size + " B";

        size /= 1024;
        if (size < 1024) return String.format("%.0f KB", size);

        size /= 1024;
        if (size < 1024) return String.format("%.2f MB", size);

        size /= 1024;
        return String.format("%.2f GB", size);
    }
}
