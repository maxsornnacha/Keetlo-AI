package com.keetlo.ai.dto;

public class StripePackageRequest {
    private String 	subscriptionPlanId;

    StripePackageRequest(){};

    public String getSubscriptionPlanId(){
        return this.subscriptionPlanId;
    }
    
}
