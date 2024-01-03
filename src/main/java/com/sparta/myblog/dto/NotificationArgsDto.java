package com.sparta.myblog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class NotificationArgsDto {

    @Schema(name = "alarmId", example = "4f3dda35-3739-406c-ad22-eed438831d66", description = "알림을 일으킨 주체의 ID")
    private String callingMemberId;

    @Schema(name = "postId", example = "4f3dda35-3739-406c-ad22-eed438831d66", description = "알림 일어난 작성 글 ID")
    private String postId;

    @Schema(name = "commentId", example = "4f3dda35-3739-406c-ad22-eed438831d66", description = "새로 남겨진 댓글 ID")
    private String commentId;
}
