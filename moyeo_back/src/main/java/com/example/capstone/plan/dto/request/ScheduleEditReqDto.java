package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.dto.common.EditActionDto;
import com.example.capstone.plan.dto.common.PlaceDetailDto;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScheduleEditReqDto {
    private List<PlaceDetailDto> originalSchedule; // 기존 일정 전체
    private List<EditActionDto> edits;             // 수정사항 리스트
}
