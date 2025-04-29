package com.example.capstone.user.dto;

import com.example.capstone.user.entity.Gender;
import com.example.capstone.user.entity.MBTI;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProfileResDto {

    private String nickname;
    private Gender gender;
    private int age;
    private MBTI mbti;
    private String profileImageUrl;
}
