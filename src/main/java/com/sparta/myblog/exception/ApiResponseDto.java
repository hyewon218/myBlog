package com.sparta.myblog.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class ApiResponseDto {
    // API result 반환을 위한 DTO
    // 성공 MSG 와 status code(상태 코드)를 반환
    private String msg;
    private int statusCode;

    @Builder
    public ApiResponseDto(String msg, int statusCode) {
        this.msg = msg;
        this.statusCode = statusCode;
    }
}
