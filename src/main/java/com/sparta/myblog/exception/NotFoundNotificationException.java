package com.sparta.myblog.exception;


import org.springframework.http.HttpStatus;

public class NotFoundNotificationException extends CustomException {

    public NotFoundNotificationException(final Long notificationId) {
        super(
            String.format("해당 알림은 존재하지 않습니다. id={%d}", notificationId),
            "올바르지 않은 알림입니다.",
            HttpStatus.NOT_FOUND,
            "5001"
        );
    }
}