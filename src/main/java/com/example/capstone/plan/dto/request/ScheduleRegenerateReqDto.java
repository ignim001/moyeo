package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.dto.response.FullScheduleResDto;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleRegenerateReqDto {
    private ScheduleCreateReqDto request;
    private FullScheduleResDto originalSchedule;}
