package com.example.capstone.matching.controller;

import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.example.capstone.matching.dto.MatchingProfileReqDto;
import com.example.capstone.matching.dto.MatchingListProfileResDto;
import com.example.capstone.matching.dto.MatchingUserProfileResDto;
import com.example.capstone.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @Operation(summary = "매칭 정보 입력 API",
            description = "일정, 목적지, 그룹, 나이대, 여행성향 입력")
    @PostMapping("/profile")
    public ResponseEntity<?> saveMatchingProfile (
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @Valid @RequestBody MatchingProfileReqDto profileRequestDto) {
        matchingService.createMatchProfile(userDetails, profileRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "특정 사용자 정보 조회 API",
            description = "결과 리스트에서 선택한 사용자 정보 조회")
    @GetMapping("/profile")
    public ResponseEntity<?> getMatchingProfile(@RequestParam String nickname) {
        MatchingUserProfileResDto matchingResultProfile = matchingService.matchingUserProfile(nickname);
        return new ResponseEntity<>(matchingResultProfile, HttpStatus.OK);
    }

    @Operation(summary = "매칭 결과 조회 API",
            description = "사용자 매칭정보 기반으로 다른 사용자 매칭")
    @GetMapping("/result")
    public ResponseEntity<?> getMatchingResult(
            @AuthenticationPrincipal CustomOAuth2User userDetails){
        List<MatchingListProfileResDto> matchingResults = matchingService.matchingResult(userDetails);
        return new ResponseEntity<>(matchingResults, HttpStatus.OK);
    }
}
