package com.example.capstone.plan.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ScheduleSaveResDto {
    private Long scheduleId;  // 저장된 Schedule ID
}
