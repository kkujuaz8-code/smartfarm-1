package com.example.smartfarmserver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    private String userId; 

    private String title;
    
    @Column(length = 1000) 
    private String content;
    private Long folderId;
    private String date;
    private String imageUrl;
}