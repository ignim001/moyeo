package com.example.capstone.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileRequestDto {

    @NotBlank
    private String nickname;

    @NotBlank
    private String gender;

    @NotNull
    private int age;

    @Nullable
    private String mbti;

}
