package com.example.capstone.plan.dto.request;


import com.example.capstone.plan.entity.CITY;
import com.example.capstone.plan.entity.Mbti;
import com.example.capstone.plan.entity.PeopleGroup;
import com.example.capstone.plan.entity.TravelStyle;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TravelPlanRequest {
    private LocalDate startDate;    // 출발일 (필수)
    private LocalDate endDate;      // 도착일 (필수)
    private List<CITY> destination;      // 목적지 (선택)(*도단위, 시단위*)
    private Mbti mbti;             // MBTI (선택)
    private List<TravelStyle> travelStyle;      // 여행 성향 (선택)
    private PeopleGroup peopleGroup;       // 여행 인원 수 (단둘이, 여럿이)
    private Long budget;
}
