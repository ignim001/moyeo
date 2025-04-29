package com.example.capstone.user.controller;

import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.example.capstone.user.dto.UserProfileReqDto;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.user.service.UserService;
import com.example.capstone.util.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "회원가입 API",
            description = "닉네임, 나이, 성별, MBTI, 이미지 입력")
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(
            @AuthenticationPrincipal CustomOAuth2User userDetails, // JWT에서 추출되 임시로 세션에 저장된 사용자 정보
            @Valid @RequestPart("userInfo") UserProfileReqDto profileRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        UserEntity user = userService.signup(userDetails, profileRequestDto, profileImage);

        // 정식 토큰 발급
        String token = jwtUtil.generateToken(user.getProviderId(), user.getEmail());
        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("token", token);
        return new ResponseEntity<>(loginInfo, HttpStatus.OK);
    }
}
