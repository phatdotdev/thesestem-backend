package com.dev.thesis_management.common.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class MimeTypeUtils {
    public static String detectMimeType(String filename, String fallback) {
        try {
            Path path = Paths.get(filename);
            String mimeType = Files.probeContentType(path);
            return Optional.ofNullable(mimeType).orElse(fallback);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static String fromExtension(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }
}
