package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public class PublicProject {
    private String projectId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;
    private String title;
    private String description;
    private String type;
    private List<String> tags;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String indexPage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mainHtmlContent;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String link;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer favoriteCount;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer remixCount;
     @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isFavorite;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private User user;
    private LocalDateTime publicAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Page> pages;

    public PublicProject() {}

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

    public LocalDateTime getPublicAt() {
        return publicAt;
    }

    public void setPublicAt(LocalDateTime publicAt) {
        this.publicAt = publicAt;
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

   public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

        public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(Integer favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public Integer getRemixCount() {
        return remixCount;
    }

    public void setRemixCount(Integer remixCount) {
        this.remixCount = remixCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

     public Boolean getIsFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setMainHtmlContent(String mainHtmlContent) {
        this.mainHtmlContent = mainHtmlContent;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

}
