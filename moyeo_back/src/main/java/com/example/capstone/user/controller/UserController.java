package com.example.capstone.controller;

import com.example.capstone.dto.oauth2.CustomOAuth2User;
import com.example.capstone.dto.request.UserProfileReqDto;
import com.example.capstone.dto.response.UserProfileResDto;
import com.example.capstone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원조회 API",
            description = "회원 수정시 화면에 출력할 회원정보 조회")
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(
            @AuthenticationPrincipal CustomOAuth2User user) {
        UserProfileResDto findUser = userService.findUser(user);
        return new ResponseEntity<>(findUser, HttpStatus.OK);
    }

    @Operation(summary = "회원수정 API",
            description = "해당 사용자의 닉네임, 성별, 나이 변경가능")
    @PutMapping("/edit")
    public ResponseEntity<?> editUser(
            @AuthenticationPrincipal CustomOAuth2User userDetails, // JWT에서 추출되 임시로 세션에 저장된 사용자 정보
            @Valid @RequestPart("userInfo") UserProfileReqDto profileRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        userService.updateProfile(userDetails, profileRequestDto, profileImage);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}