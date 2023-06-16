package com.sparta.myblog.dto;

import lombok.Getter;

@Getter
public class BlogRequestDto {
    private String title;
    private String author;
    private String contents;
    private String password;
}