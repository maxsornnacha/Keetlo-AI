package com.keetlo.ai.model;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserSubscriptionPlan {
   @JsonProperty("userSubscriptionPlanId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userSubscriptionPlanId;

    @JsonProperty("userId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    @JsonProperty("subscriptionPlanId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String subscriptionPlanId;

    @JsonProperty("startDate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate startDate;

    @JsonProperty("endDate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDate endDate;

    public UserSubscriptionPlan(){};

     // Getters
    public String getUserSubscriptionPlanId() {
        return userSubscriptionPlanId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // Setters
    public void setUserSubscriptionPlanId(String userSubscriptionPlanId) {
        this.userSubscriptionPlanId = userSubscriptionPlanId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSubscriptionPlanId(String subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // Additional Method
    public String createUserSubscriptionPlanId() {
        return UUID.randomUUID().toString();
    }

}
