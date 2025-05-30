package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.entity.City;
import com.example.capstone.user.entity.MBTI;
import com.example.capstone.plan.entity.PeopleGroup;
import com.example.capstone.matching.entity.TravelStyle;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScheduleCreateReqDto {
    private LocalDate startDate;       // 출발일 (필수)
    private LocalDate endDate;         // 도착일 (필수)
    private City destination;          // 목적지 (선택)
    private MBTI mbti;                 // MBTI (선택)
    private TravelStyle travelStyle;   // 여행 성향 (선택)
    private PeopleGroup peopleGroup;   // 여행 인원 수
    private Long budget;
}
