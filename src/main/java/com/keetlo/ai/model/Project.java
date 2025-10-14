package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Project {
    private String projectId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;
    private String title;
    private String description;
    private String type;
    private List<String> tags;
    private LocalDateTime createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime updatedAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Message> messages;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isPublic;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String indexPage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mainHtmlContent;


    public Project() {}

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTags(){
        return tags;
    }

   public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

            public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }


    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }
    public String getIndexPage() {
        return indexPage;
    }

    public void setIndexPage(String indexPage) {
        this.indexPage = indexPage;
    }

      public String getMainHtmlContent() {
        return mainHtmlContent;
    }

    public void setMainHtmlContent(String mainHtmlContent) {
        this.mainHtmlContent = mainHtmlContent;
    }



    //Helper method
    public String createProjectId() {
        return UUID.randomUUID().toString();
    }

    public String get(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }
}
