package com.keetlo.ai.dto;

import java.util.List;

import com.keetlo.ai.model.File;

public class CreateProjectReq {
    private String input;
    private List<File> files;

    public CreateProjectReq() {}
    public CreateProjectReq(String input, List<File> files) {
        this.input = input;
        this.files = files;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

}
