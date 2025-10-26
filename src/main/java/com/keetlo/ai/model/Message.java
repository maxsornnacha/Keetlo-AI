package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Message {
    private String projectMessageId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String projectId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;
    private String role; // "user" or "ai"
    private String message;
    private LocalDateTime createdAt;

    public enum Role {
        USER,
        AI;
    }

    public Message() {}

    public String getProjectMessageId() {
        return projectMessageId;
    }

    public void setProjectMessageId(String projectMessageId) {
        this.projectMessageId = projectMessageId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    //Helper method
    public String createProjectChatId() {
        return UUID.randomUUID().toString();
    }
}
