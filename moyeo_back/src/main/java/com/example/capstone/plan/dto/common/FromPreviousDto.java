package com.example.capstone.plan.dto.common;

import com.example.capstone.plan.entity.FromPrevious;
import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FromPreviousDto {
    private Integer walk;              // 도보 이동 시간 (분 단위)
    private Integer publicTransport;   // 대중교통 이동 시간
    private Integer car;               // 차량 이동 시간

    public static FromPreviousDto fromEntity(FromPrevious entity) {
        if (entity == null) return null;
        return FromPreviousDto.builder()
                .walk(entity.getWalk())
                .publicTransport(entity.getPublicTransport())
                .car(entity.getCar())
                .build();
    }
}
