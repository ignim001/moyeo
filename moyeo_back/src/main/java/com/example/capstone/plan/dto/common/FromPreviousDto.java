package com.example.capstone.plan.dto.common;


import com.example.capstone.plan.entity.FromPrevious;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FromPreviousDto {
    private Integer walk; // 도보 이동 시간 (분 단위)
    private Integer publicTransport; // 대중교통 이동 시간
    private Integer car; // 차량 이동 시간

    public static FromPreviousDto fromEntity(FromPrevious entity) {
        if (entity == null) return null;
        return new FromPreviousDto(entity.getWalk(), entity.getPublicTransport(), entity.getCar());
    }
}
