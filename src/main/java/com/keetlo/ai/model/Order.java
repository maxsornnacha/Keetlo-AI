package com.keetlo.ai.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {
    @JsonProperty("invoice")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String invoice;

    @JsonProperty("userId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

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

    @JsonProperty("status")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PaymentStatus status = PaymentStatus.PENDING;

    @JsonProperty("createdAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;

    @JsonProperty("status")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime updatedAt;

    // Enum for status
    public enum PaymentStatus {
        PAID,
        UNPAID,
        NO_PAYMENT_REQUIRED,
        PENDING
    }

    // Getters and Setters
    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
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
}

