package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.dto.request.ScheduleCreateReqDto;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRegenerateReqDto {
    private ScheduleCreateReqDto request;
    private List<String> excludedNames;          // 제외할 장소 이름 리스트
}
