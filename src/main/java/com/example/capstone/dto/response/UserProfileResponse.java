package com.example.capstone.dto.response;

import com.example.capstone.entity.Gender;
import com.example.capstone.entity.MBTI;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProfileResponse {

    private String nickname;
    private Gender gender;
    private int age;
    private MBTI mbti;
    private String profileImageUrl;
}
