package com.example.capstone.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ErrorDetails {

    private String message;
    private String path;
    // 필요시 추가정보 입력
    private Map<String, Object> extra;

}
