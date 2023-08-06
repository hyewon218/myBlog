package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Comment;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CommentResponseDto {

  private long commentId;
  private long postId;
  private String content;
  private String username;
  private Integer likeCount;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private String imageUrl;

  public CommentResponseDto(Comment comment) {
    this.commentId = comment.getCommentId();
    this.postId = comment.getPost().getId();
    this.content = comment.getContent();
    this.username = comment.getUser().getUsername();
    //this.likeCount = comment.getCommentLikes().size();
    this.createdAt = comment.getCreatedAt();
    this.modifiedAt = comment.getModifiedAt();
    this.imageUrl = comment.getUser().getImageUrl(); // 댓글 프로필 사진
  }
}