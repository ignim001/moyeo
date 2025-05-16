package com.example.capstone.plan.dto.request;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRecreateReqDto {
    private ScheduleCreateReqDto request;
    private List<String> excludedNames;          // 제외할 장소 이름 리스트
}
