package com.sparta.myblog.dto;

import lombok.Getter;

@Getter
public class BlogDeleteResponseDto {
    private boolean success;

    public BlogDeleteResponseDto(boolean success) {
        this.success = success;
    }
}
