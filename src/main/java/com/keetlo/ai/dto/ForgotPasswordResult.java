package com.keetlo.ai.dto;

public class ForgotPasswordResult {
    private boolean success;
    private String message;
    private String token; // optional if you want to return it for debugging

    public ForgotPasswordResult(boolean success, String message, String token) {
        this.success = success;
        this.message = message;
        this.token = token;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
}
