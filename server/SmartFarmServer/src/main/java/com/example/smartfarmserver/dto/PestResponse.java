package com.example.smartfarmserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PestResponse {
    private String name;
    private String description;
    private String solution;
    private String imgUrl; 
}