package com.example.smartfarmserver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member")
public class Member {

    @Id
    @Column(name = "member_id", unique = true, nullable = false)
    private String memberId; 

    private String password;
    private String name;
    private String email;
}