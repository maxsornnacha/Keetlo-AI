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

    public void saveBase64File(String base64File, String filePath) throws IOException {
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
            throw new IllegalArgumentException("File size exceeds 1MB limit");
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
}