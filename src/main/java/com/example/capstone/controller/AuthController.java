package com.example.capstone.controller;

import com.example.capstone.dto.oauth2.CustomOAuth2User;
import com.example.capstone.dto.request.UserProfileRequestDto;
import com.example.capstone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "회원가입 API",
            description = "닉네임, 나이, 성별, MBTI, 이미지 입력")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(
            @AuthenticationPrincipal CustomOAuth2User userDetails, // JWT에서 추출되 임시로 세션에 저장된 사용자 정보
            @Valid @RequestPart("userInfo") UserProfileRequestDto profileRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        userService.signup(userDetails, profileRequestDto, profileImage);
        return ResponseEntity.ok("회원가입 성공");
    }
}
