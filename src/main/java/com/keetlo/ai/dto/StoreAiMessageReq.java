package com.keetlo.ai.dto;

public class StoreAiMessageReq {
    private String projectId;
    private String message;

    public StoreAiMessageReq() {
    }

    public StoreAiMessageReq(String projectId, String userId, String message) {
        this.projectId = projectId;
        this.message = message;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
