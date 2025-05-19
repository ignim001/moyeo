package com.example.capstone.user.entity;

import com.example.capstone.matching.entity.MatchingProfile;
import com.example.capstone.util.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserEntity extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private MatchingProfile matchingProfile;

    // 사용자 식별자 (kakao + provider id)
    @Column(nullable = false)
    private String providerId;

    @Column(nullable = false, unique = true)
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

    // 리스트 조회시 상대방 사용자가 회원 탈퇴시 사용할 정보
    public static UserEntity deletedUserPlaceholder() {
        return UserEntity.builder()
                .id(-1L)
                .nickname("알 수 없음") // 또는 "탈퇴한 사용자"
                .profileImageUrl("default-profile.png") // 기본 이미지 경로
                .build();
    }
}
