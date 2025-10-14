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

    // Additional Method
    public String createUserSubscriptionPlanId() {
        return UUID.randomUUID().toString();
    }

}
