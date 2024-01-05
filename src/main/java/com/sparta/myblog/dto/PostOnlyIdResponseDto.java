package com.sparta.myblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostOnlyIdResponseDto {

    @Schema(name = "postId", example = "4f3dda35-3739-406c-ad22-eed438831d66", description = "게시글 ID")
    private String postId;

    public static PostOnlyIdResponseDto of(String postId) {
        return PostOnlyIdResponseDto.builder()
            .postId(postId)
            .build();
    }
}