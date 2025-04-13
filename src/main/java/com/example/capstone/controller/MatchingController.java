package com.example.capstone.controller;

import com.example.capstone.dto.oauth2.CustomOAuth2User;
import com.example.capstone.dto.request.MatchingProfileRequest;
import com.example.capstone.dto.response.MatchingProfileResponse;
import com.example.capstone.service.MatchingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/profile")
    public ResponseEntity<?> saveMatchingProfile (
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @Valid @RequestBody MatchingProfileRequest profileRequestDto) {
        matchingService.createMatchProfile(userDetails, profileRequestDto);
        return ResponseEntity.ok("매칭정보 생성 성공");
    }

    @GetMapping("/profile")
    public ResponseEntity<MatchingProfileResponse> getMatchingProfile(@RequestParam String nickname) {
        MatchingProfileResponse matchingResultProfile = matchingService.getMatchingResultProfile(nickname);
        return ResponseEntity.ok(matchingResultProfile);
    }
}
