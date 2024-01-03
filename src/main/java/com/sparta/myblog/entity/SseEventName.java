package com.sparta.myblog.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SseEventName {
    NOTIFICATION_LIST("notificationList");

    private final String value;

    public static SseEventName getEnumFromValue(String name) {
        for (SseEventName e : SseEventName.values()) {
            if (e.getValue().equals(name)) {
                return e;
            }
        }
        return null;// not found
    }

}
