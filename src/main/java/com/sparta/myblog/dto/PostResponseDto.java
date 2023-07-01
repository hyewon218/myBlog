package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Post;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class PostResponseDto {
    private Long id;
    private String title;
    private String content;
    // 생성시간, 수정시간 추가
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<CommentResponseDto> commentList;
    private String username;

    // 들어온 값으로 필드들을 조회해서 넣어주게 됨
    // 게시글 조회, 생성, 수정에서 사용
    public PostResponseDto(Post post) {
        this.id = post.getPostId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        this.username = post.getUser().getUsername();
        //  Stream 처리를 통해 Comment 를 CommentResponseDto 로 변환 (CommentResponseDto 생성자로 Comment 객체가 들어간 메서드생성)
        this.commentList = post.getCommentList().stream().map(CommentResponseDto::new).collect(Collectors.toList());
    }
}
