package com.example.smartfarmapp.model;

import com.google.gson.annotations.SerializedName;

public class PestResponse {
    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("solution")
    private String solution;

    @SerializedName("imgUrl")
    private String imgUrl;

    public PestResponse(String name, String description, String solution, String imgUrl) {
        this.name = name;
        this.description = description;
        this.solution = solution;
        this.imgUrl = imgUrl;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSolution() { return solution; }
    public String getImgUrl() { return imgUrl; }
}