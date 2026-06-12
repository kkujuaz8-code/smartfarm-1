package com.example.smartfarmserver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomDictionary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // 식물 이름
    private String description; // 한 줄 설명
    private String imageUrl;    // 이미지 경로

    // 🌟 추가할 상세 정보 칸
    private String waterCycle;  // 물 주기 (예: 주 1회)
    private String repotCycle;  // 분갈이 (예: 2년마다)
    private String sunlight;    // 햇빛 (예: 반양지)
    
    // 🌟 병해충 정보 칸 (병해충 도감으로 쓸 경우)
    private String solution;    // 해결 방법
}