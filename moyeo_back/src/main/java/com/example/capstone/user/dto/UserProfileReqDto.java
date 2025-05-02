package com.example.capstone.dto.request;

import com.example.capstone.entity.Gender;
import com.example.capstone.entity.MBTI;
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
