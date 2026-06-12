package com.example.smartfarmapp.model;

import com.google.gson.annotations.SerializedName;

public class PlantResponse {
    @SerializedName("name") private String name;
    @SerializedName("description") private String description;
    @SerializedName("info") private String info;
    @SerializedName("imgUrl") private String imgUrl;
    @SerializedName("waterCycle") private String waterCycle;
    @SerializedName("repotCycle") private String repotCycle;
    @SerializedName("sunlight") private String sunlight;

    // 🌟 반드시 7개 파라미터를 받는 생성자가 있어야 합니다.
    public PlantResponse(String name, String description, String info, String imgUrl, String waterCycle, String repotCycle, String sunlight) {
        this.name = name;
        this.description = description;
        this.info = info;
        this.imgUrl = imgUrl;
        this.waterCycle = waterCycle;
        this.repotCycle = repotCycle;
        this.sunlight = sunlight;
    }

    // Getter들...
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImgUrl() { return imgUrl; }
    public String getWaterCycle() { return waterCycle; }
    public String getRepotCycle() { return repotCycle; }
    public String getSunlight() { return sunlight; }
}