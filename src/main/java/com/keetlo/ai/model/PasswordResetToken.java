package com.keetlo.ai.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PasswordResetToken {
    @JsonProperty("password_reset_token_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String passwordResetTokenId;

    @JsonProperty("user_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

    @JsonProperty("token")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;

    @JsonProperty("expires_at")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String expiresAt;

    @JsonProperty("created_at")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdAt;

    public PasswordResetToken(){};

    //Getter
    public String getPasswordResetTokenId(){
        return this.passwordResetTokenId;
    }
    public String getUserId(){
        return this.userId;
    }
    public String getToken(){
        return this.token;
    }
    public String getExpiresAt(){
        return this.expiresAt;
    }
    public String getCreatedAt(){
        return this.createdAt;
    }

    // Additional Method
    public String createPasswordResetTokenId() {
        return UUID.randomUUID().toString();
    }

     public String createToken() {
        return UUID.randomUUID().toString();
    }
}
