package com.sparta.myblog.controller;

import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.dto.UserInfoDto;
import com.sparta.myblog.dto.UserProfileResponseDto;
import com.sparta.myblog.dto.UserProfileRequestDto;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @GetMapping("/user/login-page")
    public String loginPage() {
        return "signup_login";
    }

    @GetMapping("/user/signup")
    public String signupPage() {
        return "signup_login";
    }

    @PostMapping("/user/signup")
    public String signup(@Valid SignupRequestDto requestDto, BindingResult bindingResult) {
        // Validation 예외처리
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        if(fieldErrors.size() > 0) {
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
            }
            return "redirect:/api/user/signup";
        }

        userService.signup(requestDto);
        log.info("회원가입이 완료되었습니다.");
        return "redirect:/api/user/login-page";
    }

    // 회원 관련 정보 받기
    @GetMapping("/user-info")
    @ResponseBody
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String username = userDetails.getUser().getUsername();
        UserRoleEnum role = userDetails.getUser().getRole();
        boolean isAdmin = (role == UserRoleEnum.ADMIN);
        String selfText = userDetails.getUser().getSelfText();

        return new UserInfoDto(username, isAdmin, selfText);
    }

    // 프로필 조회
    @GetMapping("/{userId}/profile")
    public String getProfile(@PathVariable Long userId, Model model) {
        UserProfileResponseDto dto = userService.getUserProfile(userId);
        model.addAttribute("user", dto);

        return "update_profile";
    }

    // 프로필 수정
    @PutMapping("/{userId}/profile")
    public UserProfileResponseDto update(@PathVariable Long userId, @RequestBody UserProfileRequestDto requestDto) {
        return userService.updateUserProfile(userId, requestDto);
    }
}

/*    // 회원가입 (postman 으로 실행)
    @PostMapping("/signup")
    public ResponseEntity<ApiResponseDto> signUp(@Valid @RequestBody SignupRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.status(201).body(new ApiResponseDto("회원가입 성공", HttpStatus.CREATED.value()));
    }
    // 로그인 (postsman 으로 실행 - JwtAuthenticationFilter 필요 X)
    @PostMapping("/login.html")
    public ResponseEntity<ApiResponseDto> login.html(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        userService.login.html(loginRequestDto);
        //JWT 생성 및 쿠키에 저장 후 Response 객체에 추가
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, jwtUtil.createToken(loginRequestDto.getUsername(), loginRequestDto.getRole()));
        return ResponseEntity.ok().body(new ApiResponseDto("로그인 성공", HttpStatus.CREATED.value()));
    }*/


