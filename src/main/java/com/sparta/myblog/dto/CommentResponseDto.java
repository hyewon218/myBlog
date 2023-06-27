package com.sparta.myblog.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentResponseDto {

    private long commentId;
    private long postId;
    private String content;
    private String username;

    @Builder
    public CommentResponseDto(long commentId, long postId, String content, String username) {
        this.commentId = commentId;
        this.postId = postId;
        this.content = content;
        this.username = username;
    }
}