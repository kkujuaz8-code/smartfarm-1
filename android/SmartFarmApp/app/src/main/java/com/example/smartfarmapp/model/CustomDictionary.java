package com.example.smartfarmapp.model;

import com.google.gson.annotations.SerializedName;

public class CustomDictionary {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("imageUrl")
    private String imageUrl;

    // Getter
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
}