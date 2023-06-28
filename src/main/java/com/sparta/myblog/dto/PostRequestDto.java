package com.sparta.myblog.dto;

import lombok.Getter;

@Getter
public class PostRequestDto {
    // Controller 에서 각 값을 전달해주면 값을 받아서 PostRequestDto 객체로 변환
    private String title;
    private String username;
    private String content;
    private String password;
}