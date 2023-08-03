package com.sparta.myblog.exception;

import com.sun.jdi.request.DuplicateRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({IllegalArgumentException.class})
  public ResponseEntity<ApiResponseDto> illegalArgumentExceptionHandler(
      IllegalArgumentException ex) {
    ApiResponseDto ApiResponseDto = new ApiResponseDto(ex.getMessage(),
        HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(
        // HTTP body
        ApiResponseDto,
        // HTTP status code
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler({DuplicateRequestException.class})
  public ResponseEntity<ApiResponseDto> DuplicateRequestException(DuplicateRequestException ex) {
    ApiResponseDto ApiResponseDto = new ApiResponseDto(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    return new ResponseEntity<>(
        // HTTP body
        ApiResponseDto,
        // HTTP status code
        HttpStatus.BAD_REQUEST
    );
  }

  @ExceptionHandler({NullPointerException.class})
  public ResponseEntity<ApiResponseDto> nullPointerExceptionHandler(NullPointerException ex) {
    ApiResponseDto ApiResponseDto = new ApiResponseDto(ex.getMessage(),
        HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<>(
        // HTTP body
        ApiResponseDto,
        // HTTP status code
        HttpStatus.NOT_FOUND
    );
  }

  @ExceptionHandler({NotFoundException.class})
  public ResponseEntity<ApiResponseDto> notFoundExceptionHandler(NotFoundException ex) {
    ApiResponseDto exceptionResponse = new ApiResponseDto(ex.getMessage(), HttpStatus.NOT_FOUND.value());
    return new ResponseEntity<>(
        // HTTP body
        exceptionResponse,
        // HTTP status code
        HttpStatus.NOT_FOUND
    );
  }

}
