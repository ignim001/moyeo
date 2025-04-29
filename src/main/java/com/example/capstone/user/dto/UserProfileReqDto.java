package com.example.capstone.user.dto;

import com.example.capstone.user.entity.Gender;
import com.example.capstone.user.entity.MBTI;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserProfileReqDto {

    @NotBlank
    private String nickname;

    @NotBlank
    private Gender gender;

    @NotNull
    private int age;
    private MBTI mbti;

}
