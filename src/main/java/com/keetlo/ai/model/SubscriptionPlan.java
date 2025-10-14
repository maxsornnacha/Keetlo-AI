package com.keetlo.ai.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SubscriptionPlan {
    @JsonProperty("subscriptionPlanId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userSubscriptionPlanId;

    @JsonProperty("name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonProperty("description")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    @JsonProperty("price")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double price;
    
    @JsonProperty("requestsPerDay")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer requestsPerDay;

    @JsonProperty("advancedFeatures")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer advancedFeatures;

    @JsonProperty("prioritySupport")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer prioritySupport;

    @JsonProperty("communitySupport")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer communitySupport;

    @JsonProperty("createdAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer createdAt;

    @JsonProperty("updatedAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer updatedAt;

    @JsonProperty("isActive")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isActive;

    @JsonProperty("packageType")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String packageType;

    @JsonProperty("mostPopular")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer mostPopular;

    @JsonProperty("isVisible")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isVisible;

    public SubscriptionPlan() {}

    public String getSubscriptionPlanId() {
        return userSubscriptionPlanId;
    }

    public void setSubscriptionPlanId(String userSubscriptionPlanId) {
        this.userSubscriptionPlanId = userSubscriptionPlanId;
    }

    //Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getRequestsPerDay() {
        return requestsPerDay;
    }

    public Integer getAdvancedFeatures() {
        return advancedFeatures;
    }
    
    public Integer getPrioritySupport() {
        return prioritySupport;
    }

    public Integer getCommunitySupport() {
        return communitySupport;
    }


    public Integer getCreatedAt() {
        return createdAt;
    }

    public Integer getUpdatedAt() {
        return updatedAt;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public String getPackageType(){
        return packageType;
    }

    public Integer getMostPopular(){
        return mostPopular;
    }

    public Integer getIsVisible(){
        return isVisible;
    }

    //Setters
     public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setRequestsPerDay(Integer requestsPerDay) {
        this.requestsPerDay = requestsPerDay;
    }

    public void setAdvancedFeatures(Integer advancedFeatures) {
        this.advancedFeatures = advancedFeatures;
    }

    public void setPrioritySupport(Integer prioritySupport) {
        this.prioritySupport = prioritySupport;
    }

    public void setCommunitySupport(Integer communitySupport) {
        this.communitySupport = communitySupport;
    }

    public void setCreatedAt(Integer createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Integer updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public void setPackageType(String packageType){
       this.packageType = packageType;
    }

    public void setMostPopular(Integer mostPopular){
        this.mostPopular = mostPopular;
    }

    public void setIsVisible(Integer isVisible){
        this.isVisible = isVisible;
    }

}
