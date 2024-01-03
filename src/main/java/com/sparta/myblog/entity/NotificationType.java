package com.sparta.myblog.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NotificationType {
    LIKE("내가 쓴 글에 좋아요가 눌렸어요!"),
    COMMENT("내가 쓴 글에 댓글이 달렸어요!"),
    PURCHASE("판매 상품에 구매요청이 왔어요!");


    private final String alarmContent;
}