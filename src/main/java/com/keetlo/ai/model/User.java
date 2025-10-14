package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty("userId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    @JsonProperty("email")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;

    @JsonProperty("password")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    @JsonProperty("confirmPassword")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String confirmPassword;

    @JsonProperty("newPassword")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String newPassword;

    @JsonProperty("token")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;

    @JsonProperty("registrationId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String registrationId;

    @JsonProperty("oauthId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String oauthId;

    @JsonProperty("firstname")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String firstname;

    @JsonProperty("lastname")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String lastname;

    @JsonProperty("avatarUrl")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String avatarUrl;

    @JsonProperty("googleId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String googleId;

    @JsonProperty("githubId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String githubId;

    @JsonProperty("createdAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime updatedAt;

    @JsonProperty("deletedAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime deletedAt;

    @JsonProperty("emailVerifiedAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime emailVerifiedAt;

    @JsonProperty("leftRequests")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer leftRequests;

     @JsonProperty("limitRequests")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer limitRequests;

    @JsonProperty("subscriptionPlanName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String subscriptionPlanName;

    @JsonProperty("isGithubConnected")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isGithubConnected;

    @JsonProperty("isGoogleConnected")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isGoogleConnected;

    public User() {}

    // Getter
    public String getUserId() {
        return this.userId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    public String getNewPassword() {
        return this.newPassword;
    }

    public String getRegistrationId(){
        return this.registrationId;
    }

    public String getOauthId(){
        return this.oauthId;
    }

    public String getToken() {
        return this.token;
    }

    public String getGoogleId() {
        return this.googleId;
    }

    public String getGithubId() {
        return this.githubId;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return this.deletedAt;
    }
    public LocalDateTime getEmailVerifiedAt() {
        return this.emailVerifiedAt;
    }

    // Setter
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    public void setEmailVerifiedAt (LocalDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }
    
    public void setLeftRequests (Integer leftRequests){
        this.leftRequests = leftRequests;
    }

    public void setLimitRequests (Integer limitRequests){
        this.limitRequests = limitRequests;
    }

    public void setSubscriptionPlanName (String subscriptionPlanName){
        this.subscriptionPlanName = subscriptionPlanName;
    }

    public void setIsGithubConnected (Integer isGithubConnected){
        this.isGithubConnected = isGithubConnected;
    }

    public void setIsGoogleConnected (Integer isGoogleConnected){
        this.isGoogleConnected = isGoogleConnected;
    }

    // Additional Method
    public String createUserId() {
        return UUID.randomUUID().toString();
    }

}
