package com.example.smartfarmserver.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    private String userId;       
    private String password;
    
    private String name;
    private String email;
}