package com.sparta.myblog.dto;

import lombok.*;

@Getter
@Setter
public class CommentRequestDto {

    private long postId; // 댓글을 남길 게시글 ID
    private String content; // 댓글 내용
}