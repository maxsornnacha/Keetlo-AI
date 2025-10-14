package com.keetlo.ai.dto;

public class CreateMessageStreamReq {
    private String input;
    private String projectId;
    private Boolean firstTry;

    public CreateMessageStreamReq() {
    }

    public CreateMessageStreamReq(String input, String projectId, boolean firstTry) {
        this.input = input;
        this.projectId = projectId;
        this.firstTry = firstTry;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Boolean isFirstTry() {
        return firstTry;
    }

    public void setFirstTry(Boolean firstTry) {
        this.firstTry = firstTry;
    }
}
