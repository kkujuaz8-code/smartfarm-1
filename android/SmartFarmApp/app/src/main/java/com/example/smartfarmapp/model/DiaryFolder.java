package com.example.smartfarmapp.model;

import com.google.gson.annotations.SerializedName;

public class DiaryFolder {
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("userId")
    private String userId;

    public DiaryFolder(String name, String userId) {
        this.name = name;
        this.userId = userId;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUserId() { return userId; }
}