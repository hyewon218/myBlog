package com.sparta.myblog.controller;

import com.sparta.myblog.dto.*;
import com.sparta.myblog.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    // 회원 가입
    @PostMapping("/signup")
    public ApiResult signup(@Valid @RequestBody SignupRequestDto requestDto) {

        userService.signup(requestDto);

        return new ApiResult("회원가입 성공", HttpStatus.OK.value()); // 성공했다는 메시지, 상태코드 와 함께 Client 에 반환하기
    }

    // 로그인
    @PostMapping("/login")
    public ApiResult login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {

        userService.login(loginRequestDto, response);

        return new ApiResult("로그인 성공", HttpStatus.OK.value()); // 로그인 성공시 ApiResult Dto 를 사용하여 성공메세지와 statusCode 를 띄움
    }
}
