package com.sparta.myblog.dto;

import com.sparta.myblog.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "알람 목록 조회 응답 DTO")
public class NotificationListResponseDto {

    private List<NotificationResponseDto> notifications;
    private long unreadCount;

    public static NotificationListResponseDto of(List<Notification> notifications, long count) {
        return NotificationListResponseDto.builder()
            .notifications(notifications.stream().map(NotificationResponseDto::of)
                .toList())
            .unreadCount(count).build();
    }
}