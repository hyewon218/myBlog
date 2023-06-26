package com.sparta.myblog.dto;

import lombok.Getter;

@Getter
public class SignupResponseDto {

    private  String message;
    private int statusCode;

    public SignupResponseDto() {
        this.message = "회원가입 성공";
        this.statusCode = 200;
    }
}
