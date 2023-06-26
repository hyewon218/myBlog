package com.sparta.myblog.dto;

public class LoginResponseDto {
    private  String message;
    private int statusCode;

    public LoginResponseDto() {
        this.message = "로그인 성공";
        this.statusCode = 200;
    }
}
