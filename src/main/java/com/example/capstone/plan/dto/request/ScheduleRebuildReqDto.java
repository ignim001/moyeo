package com.example.capstone.plan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRebuildReqDto {
    private List<String> names;  // 해당 하루에 포함된 장소 이름들만 전달
}
