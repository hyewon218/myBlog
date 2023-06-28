package com.sparta.myblog.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.myblog.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // NON_NULL 인 것들만 Json 형태로 변환
public class PostResponseDto {
    // Post 객체에는 password 가 있는데 password 는 절대 노출이 되면 안 되기 때문에 Dto 생성자에서는 변환해 주지 않는다.
    // (Dto 를 쓰는 이유)
    private  Boolean success;
    private Long id;
    private String title;
    private String username;
    private String content;
    // 생성시간, 수정시간 추가
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    // 들어온 값으로 필드들을 조회해서 넣어주게 들
    // 게시글 조회, 생성, 수정에서 사용
    public PostResponseDto(Post post) {
        this.id = post.getPostId();
        this.title = post.getTitle();
        this.username = post.getUser().getUsername();
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
    }

    // 삭제 여부 표시하기 위해 매개변수 필드를 다르게 하여 생성자 추가로 만들어 줌
    // @JsonInclude(JsonInclude.Include.NON_NULL)
    //  -> success 값만 사용하는데 다른 필드들은 다 null 로 응답이 내려가므로
    //     null 로 응답이 내려가지 않도록 null 인 것들은 응답 Json 에서 빼주는 역할
    public PostResponseDto(Boolean success) {
        this.success = success;
    }
}
