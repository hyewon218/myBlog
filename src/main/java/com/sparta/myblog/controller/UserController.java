package com.sparta.myblog.controller;

import com.sparta.myblog.dto.*;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // 회원 관련 정보 받기
    @GetMapping("/auth-info")
    @ResponseBody
    // principal  :  사용자 식별, Username/Password 방식으로 인증할 때 일반적으로 UserDetails 인스턴스
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        // Authentication 의 Principal 에 저장된 UserDetailsImpl 을 가져옵니다.
        String username = userDetails.getUser().getUsername();
        UserRoleEnum role = userDetails.getUser().getRole();
        boolean isAdmin = (role == UserRoleEnum.ADMIN);

        return new UserInfoDto(username, isAdmin);
    }
}
