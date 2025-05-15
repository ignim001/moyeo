package com.example.capstone.plan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRebuildReqDto {
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ScheduleNameBlock> schedule;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleNameBlock {
        private String dayLabel;       // ex: "1일차", UI 출력용 라벨
        private List<String> names;    // 해당 일자에 포함된 장소 이름들
    }
}
