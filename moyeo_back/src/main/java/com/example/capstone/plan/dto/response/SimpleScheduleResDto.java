package com.example.capstone.plan.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SimpleScheduleResDto {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String dday;
}
