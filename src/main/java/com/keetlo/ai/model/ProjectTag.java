package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ProjectTag {
    private String projectTagId;
    private String projectId;
    private String tag;
    private LocalDateTime createdAt;

    public ProjectTag() {}

    public String getProjectTagId() {
        return projectTagId;
    }

    public void setProjectTagId(String projectTagId) {
        this.projectTagId = projectTagId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

       // Additional Method
    public String createProjectTagId() {
        return UUID.randomUUID().toString();
    }
}
