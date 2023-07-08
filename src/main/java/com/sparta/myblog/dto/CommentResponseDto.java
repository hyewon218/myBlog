package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Comment;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CommentResponseDto {

    private long commentId;
    private long postId;
    private String content;
    private String username;

    // Entity -> Dto
    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getCommentId();
        this.postId = comment.getPost().getPostId();
        this.content = comment.getContent();
        this.username = comment.getUser().getUsername();
    }
}