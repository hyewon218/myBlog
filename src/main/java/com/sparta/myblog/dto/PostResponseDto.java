package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.PostImage;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class PostResponseDto {
  private Long id;
  private String title;
  private String content;
  private String username;
  private Integer likeCount;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;
  private List<CommentResponseDto> commentList;
  private String profileImageUrl;
  private List<PostImage> imageUrlList;

  // 들어온 값으로 필드들을 조회해서 넣어주게 됨
  // 게시글 조회, 생성, 수정에서 사용
  public PostResponseDto(Post post) {
    this.id = post.getId();
    this.title = post.getTitle();
    this.content = post.getContent();
    this.createdAt = post.getCreatedAt();
    this.modifiedAt = post.getModifiedAt();
    this.username = post.getUser().getUsername();
    //this.likeCount = post.getPostLikes().size();
    this.profileImageUrl = post.getUser().getImageUrl();
    this.imageUrlList = post.getImagetList();
    this.commentList = post.getCommentList().stream().map(CommentResponseDto::new)
        .sorted(Comparator.comparing(CommentResponseDto::getCreatedAt).reversed()) // 작성날짜 내림차순
        .toList();
  }
}
