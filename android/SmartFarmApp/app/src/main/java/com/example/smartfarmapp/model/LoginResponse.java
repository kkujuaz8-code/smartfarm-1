package com.example.smartfarmapp.model;

public class LoginResponse {
    private boolean success;
    private String message;
    private String userId; // 🌟 핵심: 서버가 주는 ID 받기

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, String userId) {
        this.success = success;
        this.message = message;
        this.userId = userId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getUserId() { return userId; }
}