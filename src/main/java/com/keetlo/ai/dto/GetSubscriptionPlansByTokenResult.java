package com.keetlo.ai.dto;

import java.util.List;

import com.keetlo.ai.model.SubscriptionPlan;

public class GetSubscriptionPlansByTokenResult {
    private boolean success;
    private String message;
    private List<SubscriptionPlan> subscriptionPlans;

    public GetSubscriptionPlansByTokenResult() {}

    //getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<SubscriptionPlan> getSubscriptionPlans() { return subscriptionPlans; }

    //setters
    public void setIsSuccess(Boolean success){
        this.success = success;
    }
    public void setMessage(String message){
        this.message = message;
    }
    public void setSubscriptionPlans(List<SubscriptionPlan> subscriptionPlans){
        this.subscriptionPlans = subscriptionPlans;
    }
}
