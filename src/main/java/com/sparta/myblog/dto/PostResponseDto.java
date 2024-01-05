package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Post;
import com.sparta.myblog.entity.PostImage;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
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
    public static PostResponseDto of(Post post) {
        return PostResponseDto.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .createdAt(post.getCreatedAt())
            .modifiedAt(post.getModifiedAt())
            .username(post.getUser().getUsername())
            .profileImageUrl(post.getUser().getImageUrl())
            .imageUrlList(post.getImagetList())
            .commentList(post.getCommentList().stream()
                // of 로 수정
                .map(CommentResponseDto::new)
                .collect(Collectors.toList()))
            .build();
    }
}
