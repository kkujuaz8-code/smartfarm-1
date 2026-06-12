package com.example.smartfarmapp.model;

public class LoginRequest {
    private String userId;
    private String password;
    private String name;  // 추가됨
    private String email; // 추가됨

    // 1. 로그인할 때 쓰는 생성자 (아이디, 비번만)
    public LoginRequest(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    // 2. 회원가입할 때 쓰는 생성자 (아이디, 비번, 이름, 이메일 다 포함)
    public LoginRequest(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}