package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Page {

    @JsonProperty("generatedPageId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String generatedPageId;

    @JsonProperty("projectMessageId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String projectMessageId;

    @JsonProperty("label")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String label; 

    @JsonProperty("path")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String path;

    @JsonProperty("htmlContent")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String htmlContent;

    @JsonProperty("height")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer height;

    @JsonProperty("width")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer width;

    @JsonProperty("top")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer top;

    @JsonProperty("left")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer left;

    @JsonProperty("createdAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;

    // ===================== Getters & Setters =====================

    public String getGeneratedPageId() {
        return generatedPageId;
    }

    public void setGeneratedPageId(String generatedPageId) {
        this.generatedPageId = generatedPageId;
    }

    public String getProjectMessageId() {
        return projectMessageId;
    }

    public void setProjectMessageId(String projectMessageId) {
        this.projectMessageId = projectMessageId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

     //Helper method
    public String createGeneratedPageId() {
        return UUID.randomUUID().toString();
    }
}
