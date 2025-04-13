package com.example.capstone.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "user")
    private MatchingProfile matchingProfile;

    // 사용자 식별자 (kakao + provider id)
    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    private MBTI mbti;

    @Column(nullable = false)
    private String profileImageUrl;

    public void updateProfile(String nickname, Gender gender, int age, MBTI mbti, String profileImageUrl) {
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.mbti = mbti;
        this.profileImageUrl = profileImageUrl;
    }
}
