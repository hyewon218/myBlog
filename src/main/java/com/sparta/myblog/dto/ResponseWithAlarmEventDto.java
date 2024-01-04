package com.sparta.myblog.dto;

import com.sparta.myblog.kafka.NotificationEvent;
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
public class ResponseWithAlarmEventDto<T> {

    private T response;
    private NotificationEvent notificationEvent;
}