package com.keetlo.ai.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Base64;
import java.util.Map;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class FileUtil {
    @Value("${file.root.directory}")
    private String FILE_ROOT_DIRECTORY;
    private static final Map<String, String> MIME_TYPE_MAP = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif",
            "image/webp", "webp");

    public FileUtil() {
    }

    public String getFileRootDirectory() {
        return FILE_ROOT_DIRECTORY;
    }

    public void saveBase64Image(String base64File, String filePath) throws IOException {
        String[] parts = base64File.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid Base64 format");
        }

        String metadata = parts[0];
        String fileString = parts[1];

        String fileType = null;
        for (String mime : MIME_TYPE_MAP.keySet()) {
            if (metadata.contains(mime)) {
                fileType = MIME_TYPE_MAP.get(mime);
                break;
            }
        }
        if (fileType == null) {
            throw new IllegalArgumentException("Unsupported file type: " + metadata);
        }

        byte[] decodedBytes = Base64.getDecoder().decode(fileString);
        int maxSize = 2 * 1024 * 1024;
        if (decodedBytes.length > maxSize) {
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }

        String fullFilePath = FILE_ROOT_DIRECTORY + filePath;
        File file = new File(fullFilePath);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(fullFilePath)) {
            fos.write(decodedBytes);
        }
    }

    public void saveBase64File(String base64File, String filePath) throws IOException {
        String[] parts = base64File.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid Base64 format");
        }

        String fileString = parts[1];

        byte[] decodedBytes = Base64.getDecoder().decode(fileString);
        int maxSize = 10 * 1024 * 1024;
        if (decodedBytes.length > maxSize) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }

        String fullFilePath = FILE_ROOT_DIRECTORY + filePath;
        File file = new File(fullFilePath);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(fullFilePath)) {
            fos.write(decodedBytes);
        }
    }

    public boolean isBase64(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        try {
            String base64String = input.contains(",") ? input.split(",")[1] : input;
            Base64.getDecoder().decode(base64String);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean deleteFile(String filePath) {
        File file = new File(FILE_ROOT_DIRECTORY + filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

    // What your backend will accept (by MIME)
    private static final java.util.Set<String> ALLOWED_MIME = java.util.Set.of(
            // images
            "image/png", "image/jpeg", "image/webp", "image/gif", "image/svg+xml",
            "image/heic", "image/heif", "image/avif");

    // Map MIME → extension used when saving
    public static final java.util.Map<String, String> MIME_TO_EXT = java.util.Map.ofEntries(
            // images
            java.util.Map.entry("image/png", "png"),
            java.util.Map.entry("image/jpeg", "jpg"),
            java.util.Map.entry("image/webp", "webp"),
            java.util.Map.entry("image/gif", "gif"),
            java.util.Map.entry("image/svg+xml", "svg"),
            java.util.Map.entry("image/heic", "heic"),
            java.util.Map.entry("image/heif", "heif"),
            java.util.Map.entry("image/avif", "avif"));

    public String pickExt(String mime, String fallbackName) {
        if (mime != null) {
            String ext = MIME_TO_EXT.get(mime.toLowerCase());
            if (ext != null)
                return ext;
        }
        // fallback: from filename
        if (fallbackName != null) {
            String name = fallbackName.replace('\\', '/');
            String base = name.substring(name.lastIndexOf('/') + 1);
            int dot = base.lastIndexOf('.');
            if (dot > 0 && dot < base.length() - 1)
                return base.substring(dot + 1).toLowerCase();
        }
        return "bin";
    }

    // helpers
    public boolean isDataUrl(String s) {
        return s != null && s.startsWith("data:");
    }

    public String mimeFromDataUrl(String s) {
        if (!isDataUrl(s))
            return null;
        int c = s.indexOf(':'), sc = s.indexOf(';');
        return (c >= 0 && sc > c) ? s.substring(c + 1, sc).toLowerCase() : null;
    }

    public boolean isImageDataUrl(String s) {
        String m = mimeFromDataUrl(s);
        return m != null && m.startsWith("image/");
    }

}