package com.example.capstone.plan.dto.request;

import com.example.capstone.plan.dto.common.EditActionDto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ScheduleEditRequest {
    private Long scheduleId;
    private List<EditActionDto> edits;
}
