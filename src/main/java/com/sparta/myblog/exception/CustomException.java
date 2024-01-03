package com.sparta.myblog.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class CustomException extends RuntimeException {

    private final String showMessage;
    private final HttpStatus httpStatus;
    private final String errorCode;

    public CustomException(final String message,
        final String showMessage,
        final HttpStatus httpStatus,
        final String errorCode) {
        super(message);
        this.showMessage = showMessage;
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}