package com.sparta.myblog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.myblog.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // NON_NULL 인 것들만 Json 형태로 변환
public class PostResponseDto {
    // Post 객체에는 password 가 있는데 password 는 절대 노출이 되면 안 되기 때문에 Dto 생성자에서는 변환해 주지 않는다.
    // (Dto 를 쓰는 이유)
    private Long id;
    private String title;
    private String username;
    private String content;
    // 생성시간, 수정시간 추가
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<CommentResponseDto> commentList;

    // 들어온 값으로 필드들을 조회해서 넣어주게 들
    // 게시글 조회, 생성, 수정에서 사용
    public PostResponseDto(Post post) {
        this.id = post.getPostId();
        this.title = post.getTitle();
        this.username = post.getUser().getUsername();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        //  Stream 처리를 통해 Comment 를 CommentResponseDto 로 변환 (CommentResponseDto 생성자로 Comment 객체가 들어간 메서드생성)
        this.commentList = post.getCommentList().stream().map(CommentResponseDto::new).collect(Collectors.toList());
    }
}
