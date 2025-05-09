package com.example.capstone.plan.dto.common;


import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EditActionDto {
    private String action; // "add", "update", "delete", "reorder"
    private int day;

    // 공통 필드
    private Integer index; // 대상 인덱스 (update/delete용)

    // reorder용
    private Integer from;
    private Integer to;

    // add/update용 (GPT 처리 대상)
    private String rawInput;
}
