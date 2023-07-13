package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private long commentId;
    private long postId;
    private String content;
    private String username;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // Entity -> Dto
    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getCommentId();
        this.postId = comment.getPost().getPostId();
        this.content = comment.getContent();
        this.username = comment.getUser().getUsername();
        this.likeCount = comment.getCommentLikes().size();
        this.createdAt = comment.getCreatedAt();
        this.modifiedAt = comment.getModifiedAt();
    }
}