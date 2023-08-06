package com.sparta.myblog.controller;

import com.sparta.myblog.dto.SignupRequestDto;
import com.sparta.myblog.dto.UserInfoDto;
import com.sparta.myblog.dto.UserProfileResponseDto;
import com.sparta.myblog.dto.UserProfileRequestDto;
import com.sparta.myblog.entity.UserRoleEnum;
import com.sparta.myblog.security.UserDetailsImpl;
import com.sparta.myblog.service.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/view/user")
public class UserController {

  private final UserServiceImpl userService;

  @GetMapping("/login-page")
  public String loginPage() {
    return "login";
  }

  @GetMapping("/signup")
  public String signupPage() {
    return "signup";
  }

  @PostMapping("/signup")
  public String signup(@Valid @ModelAttribute SignupRequestDto requestDto,
      BindingResult bindingResult) throws IOException {
    // Validation 예외처리
    List<FieldError> fieldErrors = bindingResult.getFieldErrors();

    if (fieldErrors.size() > 0) {
      for (FieldError fieldError : bindingResult.getFieldErrors()) {
        log.error(fieldError.getField() + " 필드 : " + fieldError.getDefaultMessage());
      }
      return "redirect:/view/user/signup";
    }
    userService.signup(requestDto);

    return "redirect:/view/user/login-page"; // 회원가입 후 로그인 페이지로
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
  @GetMapping("/profile")
  public String profile(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
    Long userId = userDetails.getUser().getId();
    UserProfileResponseDto dto = userService.getUserProfile(userId);
    model.addAttribute("user", dto);

    return "profile";
  }

  // 프로필 수정 (PutMapping 하면 오류남..ajax 가 안되나?)
  @PostMapping("/profile")
  public String modifyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
      @ModelAttribute UserProfileRequestDto requestDto) throws IOException {
    Long userId = userDetails.getUser().getId(); // 사용자 id
    userService.updateUserProfile(userId, requestDto);

    return "redirect:/view/user/profile"; // 수정 후 조회 화면에서 데이터 보여주기
  }
}
