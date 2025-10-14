package com.keetlo.ai.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


public class OtpCode {
    @JsonProperty("otpCodeId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String otpCodeId;

    @JsonProperty("userId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId; 

    @JsonProperty("email")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;

    @JsonProperty("otpCode")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String otpCode;

    @JsonProperty("type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OtpType type;

    @JsonProperty("expriesAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime expiresAt;

    @JsonProperty("used")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer used = 0;

    @JsonProperty("attempts")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int attempts = 0;

    @JsonProperty("createdAt")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;

    public enum OtpType {
    EMAIL, SMS
    }

    public OtpCode(){}

    //Getter
    public String getOtpCodeId(){
        return this.otpCodeId;
    }
    public String getUserId(){
        return this.userId;
    }
    public String getEmail(){
        return this.email;
    }
    public String getOtpCode(){
        return this.otpCode;
    }
    public OtpType getType(){
        return this.type;
    }
    public LocalDateTime getExpiresAt(){
        return this.expiresAt;
    }
    public Integer getUsed(){
        return this.used;
    }
    public Integer getAttempts(){
        return this.attempts;
    }
    public LocalDateTime getCreatedAt(){
        return this.createdAt;
    }

    // Additional Method
    public String createOtpCodeId() {
        return UUID.randomUUID().toString();
    }
}
