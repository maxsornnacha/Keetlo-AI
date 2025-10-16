package com.keetlo.ai.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {
    @JsonProperty("receiptId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String receiptId;

    @JsonProperty("userId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    @JsonProperty("name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonProperty("email")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;

    @JsonProperty("subscriptionPlanId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String subscriptionPlanId;

    @JsonProperty("stripeSessionId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String stripeSessionId;

    @JsonProperty("stripePaymentIntentId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String stripePaymentIntentId;

    @JsonProperty("amount")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal amount;

    @JsonProperty("currency")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String currency = "usd";

    @JsonProperty("currencySymbol")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String currencySymbol;

    @JsonProperty("status")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaymentStatus status = PaymentStatus.PENDING;

    @JsonProperty("createdAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime updatedAt;

    @JsonProperty("planName")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String planName;

    @JsonProperty("planDescription")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String planDescription;

   @JsonProperty("startDate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime starDate;

    @JsonProperty("endDate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime endDate;

    public enum PaymentStatus {
    PENDING, PAID, UNPAID, FAILED, CANCELED;
    public static PaymentStatus fromDb(String s) {
        if (s == null) return PENDING;
        String v = s.trim().toUpperCase();
        switch (v) {
            case "SUCCESS": case "SUCCEEDED": case "COMPLETED": return PAID;
            case "CANCELLED": return CANCELED;
        }
        try {
            return PaymentStatus.valueOf(v);
        } catch (IllegalArgumentException ex) {
            return PENDING;
        }
    }
}

    // Getters and Setters
    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

     public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

     public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubscriptionPlanId() {
        return subscriptionPlanId;
    }

    public void setSubscriptionPlanId(String subscriptionPlanId) {
        this.subscriptionPlanId = subscriptionPlanId;
    }

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
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


    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getPlanDescription() {
        return planDescription;
    }

    public void setPlanDescription(String planDescription) {
        this.planDescription = planDescription;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

   public LocalDateTime getStartDate() {
        return starDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.starDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
}

