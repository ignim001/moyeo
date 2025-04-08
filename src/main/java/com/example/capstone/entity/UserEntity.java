package com.example.capstone.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 식별자 (kakao + provider id)
    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private int age;

    @Column(nullable = true)
    private String mbti;

    @Column(nullable = false)
    private String profileImageUrl;

    public void changeProfile(String nickname, String gender, int age, String mbti, String profileImageUrl) {
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.mbti = mbti;
        this.profileImageUrl = profileImageUrl;
    }
}
