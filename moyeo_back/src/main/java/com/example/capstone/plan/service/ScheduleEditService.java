package com.example.capstone.plan.service;


import com.example.capstone.plan.dto.common.EditActionDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import com.example.capstone.plan.dto.request.ScheduleEditRequest;
import com.example.capstone.plan.dto.response.ScheduleDetailFullResponse;
import com.example.capstone.util.gpt.GptCostAndTimePromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleEditService {

    private final PlaceUpdateService placeUpdateService;
    private final GptCostAndTimePromptBuilder costAndTimePromptBuilder;
    private final OpenAiClient openAiClient;

    /**
     * 편집 요청을 받아 일정 수정 처리
     */
    public ScheduleDetailFullResponse applyEditRequest(
            ScheduleEditRequest request,
            List<PlaceDetailDto> originalSchedule
    ) throws Exception {
        List<PlaceDetailDto> currentSchedule = new ArrayList<>(originalSchedule);

        for (EditActionDto action : request.getEdits()) {
            switch (action.getAction()) {
                case "add" -> {
                    PlaceDetailDto added = placeUpdateService.resolveNewPlace(action.getRawInput());
                    currentSchedule.add(action.getIndex(), added);
                }
                case "update" -> {
                    PlaceDetailDto updated = placeUpdateService.resolveNewPlace(action.getRawInput());
                    currentSchedule.set(action.getIndex(), updated);
                }
                case "delete" -> {
                    currentSchedule.remove(action.getIndex());
                }
                case "reorder" -> {
                    currentSchedule = reorderList(currentSchedule, action.getFrom(), action.getTo());
                }
                default -> throw new IllegalArgumentException("알 수 없는 액션 타입: " + action.getAction());
            }
        }

        // ✅ 모든 수정 반영 후 GPT에 예산 및 이동시간 요청 (한 번만 실행)
        String costPrompt = costAndTimePromptBuilder.build(currentSchedule);
        String gptResponse = openAiClient.callGpt(costPrompt);
        List<ScheduleDetailFullResponse.PlaceResponse> parsed = costAndTimePromptBuilder.parseGptResponse(gptResponse, currentSchedule);

        return new ScheduleDetailFullResponse(parsed);
    }

    private List<PlaceDetailDto> reorderList(List<PlaceDetailDto> list, int from, int to) {
        List<PlaceDetailDto> newList = new ArrayList<>(list);
        PlaceDetailDto target = newList.remove(from);
        newList.add(to, target);
        return newList;
    }
}
