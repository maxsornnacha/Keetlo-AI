package com.keetlo.ai.dto;

import java.util.List;

import com.keetlo.ai.model.File;

public class CreateMessageStreamReq {
    private String input;
    private String projectId;
    private Boolean firstTry;
    private List<File> files;

    public CreateMessageStreamReq() {
    }

    public CreateMessageStreamReq(String input, String projectId, boolean firstTry, List<File> files) {
        this.input = input;
        this.projectId = projectId;
        this.firstTry = firstTry;
        this.files = files;
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

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

}
