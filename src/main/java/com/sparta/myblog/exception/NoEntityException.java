package com.sparta.myblog.exception;

public class NoEntityException extends BusinessException{

    public NoEntityException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public NoEntityException(ErrorCode errorCode) {
        super(errorCode);
    }
}