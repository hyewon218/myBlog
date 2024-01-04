package com.sparta.myblog.kafka;

import com.sparta.myblog.entity.NotificationArgs;
import com.sparta.myblog.entity.NotificationType;
import com.sparta.myblog.entity.SseEventName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private NotificationType type;
    private NotificationArgs args;
    private Long userId;
    private SseEventName eventName;
}