package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

public class File {

    private String projectFileId;
    private String fileName;
    private String fileType;
    private Integer fileSize;
    private String fileUrl;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String base64;
    private LocalDateTime createdAt;

    // --- Constructors ---
    public File() {
    }

    public File(String fileName, String fileType, Integer fileSize, String base64) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.base64 = base64;
    }

    // --- Getters and Setters ---
        public String getProjectFileId() {
        return projectFileId;
    }

    public void setProjectFileId(String projectFileId) {
        this.projectFileId = projectFileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

      public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    // --- Optional: convenience toString() ---
   //Helper method
    public String createProjectFileId() {
        return UUID.randomUUID().toString();
    }

}
