package com.example.capstone.plan.controller;


import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.request.*;
import com.example.capstone.plan.dto.response.FullScheduleResDto.DailyScheduleBlock;
import com.example.capstone.plan.dto.response.ScheduleSaveResDto;
import com.example.capstone.plan.dto.response.FullScheduleResDto;
import com.example.capstone.plan.dto.response.SimpleScheduleResDto;
import com.example.capstone.plan.entity.TravelSchedule;
import com.example.capstone.plan.service.*;
import com.example.capstone.user.service.UserService;
import com.example.capstone.util.gpt.GptCostAndTimePromptBuilder;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Tag(name = "Schedule API", description = "여행 일정 생성, 수정, 저장 관련 API")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final GptCostAndTimePromptBuilder promptBuilder;
    private final OpenAiClient openAiClient;
    private final ScheduleEditService scheduleEditService;
    private final ScheduleRefinerService scheduleRefinerService;
    private final ScheduleRebuildService scheduleRebuildService;





    /**
     * ✅ GET 방식 - 저장된 scheduleId 기반 상세보기
     */
    @Operation(summary = "스케줄 상세조회", description = "저장된 스케줄 ID로 전체 일정을 조회합니다.")
    @GetMapping("/full/{scheduleId}")
    public ResponseEntity<FullScheduleResDto> getFullDetail(@PathVariable Long scheduleId) {
        try {
            List<PlaceDetailDto> places = scheduleService.getPlacesFromDatabase(scheduleId);
            TravelSchedule travelSchedule = scheduleService.getScheduleById(scheduleId);
            FullScheduleResDto response = scheduleService.convertToBlockStructure(places, travelSchedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @Operation(summary = "내 여행 목록 조회", description = "여행 제목, 시작일, 종료일을 조회합니다.")
    @GetMapping("/list")
    public ResponseEntity<List<SimpleScheduleResDto>> getScheduleList(
            @AuthenticationPrincipal CustomOAuth2User userDetails) {
        try {
            Long userId = userDetails.getUserId();
            List<SimpleScheduleResDto> response = scheduleService.getSimpleScheduleList(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @Operation(summary = "GPT 기반 여행일정 생성", description = "MBTI, 여행 성향, 예산 등을 기반으로 여행 일정을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<FullScheduleResDto> createSchedule(@RequestBody ScheduleCreateReqDto request) {
        try {
            FullScheduleResDto response = scheduleService.generateFullSchedule(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @Operation(summary = "일정 편집", description = "하루치 일정에 대해 추가, 수정, 삭제, 순서 변경을 한번에 반영합니다.")
    @PatchMapping("/edit")
    public ResponseEntity<DailyScheduleBlock> editDaySchedule(
            @RequestBody ScheduleEditReqDto request
    ) throws Exception {
        DailyScheduleBlock result = scheduleEditService.applyEditToDay(request);
        return ResponseEntity.ok(result);
    }



    @Operation(summary = "추천 일정 저장", description = "사용자가 확정한 여행 일정을 데이터베이스에 저장합니다.")
    @PostMapping("/save")
    public ResponseEntity<ScheduleSaveResDto> saveSchedule(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @RequestBody ScheduleSaveReqDto request) {
        Long userId = userDetails.getUserId();
        ScheduleSaveResDto response = scheduleService.saveSchedule(request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "기존 일정을 제외한 일정 재생성", description = "create로 받은 일정에서 장소들을 제외하고 새로운 일정을 생성합니다.")
    @PostMapping("/regenerate")
    public ResponseEntity<FullScheduleResDto> regenerate(@RequestBody ScheduleRegenerateReqDto request) {
        try {
            FullScheduleResDto response = scheduleService.regenerateSchedule(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
    @Operation(summary = "전체 일정 리빌딩", description = "수정된 장소 리스트를 기반으로 하루 일정이 아닌 전체 일정을 리빌딩합니다.")
    @PostMapping("/rebuild")
    public ResponseEntity<FullScheduleResDto> rebuildSchedule(@RequestBody ScheduleRebuildReqDto request) {
        try {
            FullScheduleResDto result = scheduleRebuildService.rebuildFullSchedule(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


}





