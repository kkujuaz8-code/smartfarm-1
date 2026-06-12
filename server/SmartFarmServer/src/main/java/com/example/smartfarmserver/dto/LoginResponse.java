package com.example.smartfarmserver.dto;

public class LoginResponse {
    private boolean success;
    private String message;
    private String userId; 

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, String userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMemberId() { 
        return userId;
    }

    public void setMemberId(String userId) {
        this.userId = userId;
    }
}