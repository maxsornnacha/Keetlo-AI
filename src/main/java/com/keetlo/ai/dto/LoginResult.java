package com.keetlo.ai.dto;

public class LoginResult {
    private boolean success;
    private String message;
    private String token;
    private boolean requiresVerification;

    public LoginResult(boolean success, String message, String token, boolean requiresVerification) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.requiresVerification = requiresVerification;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public boolean isRequiresVerification() { return requiresVerification; }
}
