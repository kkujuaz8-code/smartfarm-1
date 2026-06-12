package com.example.smartfarmapp.model;

import com.google.gson.annotations.SerializedName;

public class DiaryEntry {
    @SerializedName("id")
    private Long id;       // 일기 고유 번호

    @SerializedName("userId")
    private String userId; // 작성자 ID

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("date")
    private String date;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("folderId")
    private Long folderId; // 🌟 [핵심 추가] 이 일기가 담길 폴더 번호!

    // 기본 생성자 (필수: Retrofit이 사용함)
    public DiaryEntry() {}

    // 생성자 (새 글 작성용)
    public DiaryEntry(String userId, String title, String content, String date, String imageUrl, Long folderId) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.date = date;
        this.imageUrl = imageUrl;
        this.folderId = folderId;
    }

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // 🌟 폴더 ID Getter/Setter (이게 있어야 서버로 전송됨)
    public Long getFolderId() { return folderId; }
    public void setFolderId(Long folderId) { this.folderId = folderId; }
}