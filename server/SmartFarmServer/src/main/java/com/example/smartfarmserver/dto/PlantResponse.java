package com.example.smartfarmserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantResponse {
    private String name;
    private String description;
    private String info;
    private String imgUrl;
    private String waterCycle;
    private String repotCycle;
    private String sunlight;
}