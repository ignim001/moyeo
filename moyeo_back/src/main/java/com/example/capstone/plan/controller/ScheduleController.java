package com.example.capstone.plan.controller;


import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.request.SaveScheduleRequest;
import com.example.capstone.plan.dto.request.ScheduleEditRequest;
import com.example.capstone.plan.dto.request.TravelPlanRequest;
import com.example.capstone.plan.dto.response.DaySummaryResponse;
import com.example.capstone.plan.dto.response.SaveScheduleResponse;
import com.example.capstone.plan.dto.response.ScheduleDetailFullResponse;
import com.example.capstone.plan.dto.response.ScheduleSimpleResponse;
import com.example.capstone.plan.service.*;
import com.example.capstone.util.gpt.GptCostAndTimePromptBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gpt/schedule/detail")
@RequiredArgsConstructor
@Tag(name = "Schedule API", description = "여행 일정 생성, 수정, 저장 관련 API")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final GptCostAndTimePromptBuilder promptBuilder;
    private final OpenAiClient openAiClient;
    private final ScheduleEditService scheduleEditService;
    private final ScheduleRefinerService scheduleRefinerService;



    /**
     * ✅ GET 방식 - 저장된 scheduleId 기반 상세보기
     */
    @Operation(summary = "스케줄 상세조회", description = "저장된 스케줄 ID로 전체 일정을 조회합니다.")
    @GetMapping("/full/{scheduleId}")
    public ResponseEntity<ScheduleDetailFullResponse> getFullDetail(@PathVariable Long scheduleId) {
        try {
            List<PlaceDetailDto> places = scheduleService.getPlacesFromDatabase(scheduleId);
            List<ScheduleDetailFullResponse.PlaceResponse> responses = places.stream()
                    .map(ScheduleDetailFullResponse.PlaceResponse::from)
                    .toList();
            return ResponseEntity.ok(new ScheduleDetailFullResponse(responses));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ScheduleDetailFullResponse(List.of()));
        }
    }

    @Operation(summary = "내 여행 목록 조회", description = "여행 제목, 시작일, 종료일을 조회합니다.")
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<ScheduleSimpleResponse>> getScheduleList(@PathVariable Long userId) {
        try {
            List<ScheduleSimpleResponse> response = scheduleService.getSimpleScheduleList(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @Operation(summary = "GPT 기반 여행일정 생성", description = "MBTI, 여행 성향, 예산 등을 기반으로 여행 일정을 생성합니다.")
    @PostMapping("/create")
    public ResponseEntity<ScheduleDetailFullResponse> createSchedule(@RequestBody TravelPlanRequest request) {
        try {
            ScheduleDetailFullResponse response = scheduleService.generateFullSchedule(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @Operation(summary = "일괄 편집", description = "추가, 수정, 삭제, 순서변경을 한번에 반영합니다.")
    @PatchMapping("/edit")
    public ResponseEntity<ScheduleDetailFullResponse> editSchedule(@RequestBody ScheduleEditRequest request) throws Exception {
        List<PlaceDetailDto> originalSchedule = scheduleRefinerService.getScheduleById(request.getScheduleId());
        ScheduleDetailFullResponse result = scheduleEditService.applyEditRequest(request, originalSchedule);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "일차별 예상비용 생성", description = "일차별 예상비용을 요약한 정보를 생성합니다.")
    @PostMapping("/day-summary")
    public ResponseEntity<List<DaySummaryResponse>> createDaySummary(@RequestBody Map<String, List<PlaceDetailDto>> groupedByDate) {
        List<DaySummaryResponse> summaries = scheduleService.createDaySummaries(groupedByDate);
        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "추천 일정 저장", description = "사용자가 확정한 여행 일정을 데이터베이스에 저장합니다.")
    @PostMapping("/save")
    public ResponseEntity<SaveScheduleResponse> saveSchedule(@RequestBody SaveScheduleRequest request) {
        SaveScheduleResponse response = scheduleService.saveSchedule(request);
        return ResponseEntity.ok(response);
    }

}
