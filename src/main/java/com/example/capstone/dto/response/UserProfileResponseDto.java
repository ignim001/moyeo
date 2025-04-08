package com.example.capstone.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProfileResponseDto {

    private String nickname;

    private String gender;

    private int age;

    private String mbti;

    private String profileImageUrl;
}
