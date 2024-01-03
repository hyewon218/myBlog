package com.sparta.myblog.entity;

import java.io.Serial;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@EqualsAndHashCode
public class NotificationArgs implements Serializable {

    @Serial
    private static final long serialVersionUID = 300L;
    // 알람 발생 시킨 멤버
    private String callingMemberNickname;
    // 알람 발생 시킨 글 id
    private String postId;
    // 알람 발생 시킨 댓글 id
    private String commentId;
    //링크 url
    private String linkUrl;
}
