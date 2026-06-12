package com.example.smartfarmapp.model; // ⚠️ 패키지명 확인!

import com.google.gson.annotations.SerializedName;

public class PestDictionary {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("imageUrl")
    private String imageUrl;

    // Getter (id는 어댑터에서 안 쓰므로 생략 가능하지만 튼튼하게 넣어둡니다)
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
}