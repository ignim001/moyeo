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
    private List<DayNameOnlyBlock> days;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayNameOnlyBlock {
        private String day; // "1일차", "2일차" (UI용)
        private List<String> names;
    }
}
