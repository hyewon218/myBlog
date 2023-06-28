package com.sparta.myblog.controller;

import com.sparta.myblog.dto.*;
import com.sparta.myblog.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    // 회원 가입
    @PostMapping("/auth/signup")
    public ApiResult signup(@Valid @RequestBody SignupRequestDto requestDto, BindingResult bindingResult) {
        // Validation 예외처리
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if(fieldErrors.size() > 0) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
        }
        userService.signup(requestDto);

        // 성공했다는 메시지, 상태코드 와 함께 Client 에 반환하기
        return new ApiResult("회원가입 성공", HttpStatus.OK.value());
    }

    // 로그인
    @PostMapping("/auth/login")
    public ApiResult login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {

        userService.login(loginRequestDto, response);

        return new ApiResult("로그인 성공", HttpStatus.OK.value()); // 로그인 성공시 ApiResult Dto 를 사용하여 성공메세지와 statusCode를 띄움
    }
}
