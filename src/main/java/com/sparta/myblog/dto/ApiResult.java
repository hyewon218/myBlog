package com.sparta.myblog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ApiResult {
    // API result 반환을 위한 DTO
    // 성공 MSG 와 status code(상태 코드)를 반환
    private String msg;
    private int statusCode;

    @Builder
    public ApiResult(String msg, int statusCode) {
        this.msg = msg;
        this.statusCode = statusCode;
    }
}
