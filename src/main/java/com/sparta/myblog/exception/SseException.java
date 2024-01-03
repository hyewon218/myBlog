package com.sparta.myblog.exception;

public class SseException extends InfraException{

    public SseException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public SseException(ErrorCode errorCode) {
        super(errorCode);
    }
}