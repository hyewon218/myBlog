package com.sparta.myblog.exception;

import lombok.Getter;

@Getter
public class InfraException extends RuntimeException{

    private final ErrorCode errorCode;

    public InfraException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public InfraException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}